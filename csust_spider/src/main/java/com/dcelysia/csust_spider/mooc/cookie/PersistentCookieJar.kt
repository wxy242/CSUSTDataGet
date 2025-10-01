package com.dcelysia.csust_spider.mooc.cookie

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.map
import kotlin.compareTo


class PersistentCookieJar : CookieJar {
    private val mmkv by lazy { MMKV.mmkvWithID(TAG) }
    private val gson = Gson()

    // 内存缓存：存不可变 List，合并时整体替换，避免并发修改
    private val memoryCache = ConcurrentHashMap<String, List<Cookie>>()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val pendingJobs = ConcurrentHashMap<String, Job>()
    private val saveDelayMs = 500L
    private val TAG = "PersistentCookieJar"

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        Log.d(TAG, "saveFromResponse: Saving cookies for host: $host")
        val now = System.currentTimeMillis()

        // 原子地合并并替换列表，避免并发修改同一实例
        memoryCache.compute(host) { _, existing ->
            // 基于现有有效 cookie 构建初始列表
            val base = existing?.filter { it.expiresAt > now }?.toMutableList()
                ?: run {
                    val json = mmkv.decodeString(host)
                    if (json != null) {
                        val type = object : TypeToken<List<SerializableCookie>>() {}.type
                        val serializableCookies: List<SerializableCookie> = gson.fromJson(json, type)
                        serializableCookies.map { it.toOkHttpCookie() }.filter { it.expiresAt > now }.toMutableList()
                    } else {
                        mutableListOf()
                    }
                }

            // 合并新的 cookies（按 name+domain+path 覆盖）
            cookies.forEach { newCookie ->
                base.removeAll { it.name == newCookie.name && it.domain == newCookie.domain && it.path == newCookie.path }
                if (newCookie.expiresAt > now) {
                    base.add(newCookie)
                }
            }

            // 返回不可变列表作为新的 map 值
            base.filter { it.expiresAt > now }
        }

        jobSave(host)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        Log.d(TAG, "loadForRequest: Loading cookies for host: $host")
        val now = System.currentTimeMillis()

        val list = memoryCache.computeIfAbsent(host) {
            val json = mmkv.decodeString(host)
            Log.d(TAG, "loadForRequest: Reading from MMKV for host: $host, json: $json")
            if (json != null) {
                val type = object : TypeToken<List<SerializableCookie>>() {}.type
                val serializableCookies: List<SerializableCookie> = gson.fromJson(json, type)
                serializableCookies.map { it.toOkHttpCookie() }
            } else {
                mutableListOf()
            }
        }

        val validList = list.filter { it.expiresAt > now }
        Log.d(TAG, "loadForRequest: Returning ${validList.size} valid cookies for host: $host")
        return validList
    }

    private fun jobSave(host: String) {
        pendingJobs[host]?.let { job ->
            if (!job.isCompleted && !job.isCancelled) {
                Log.d(TAG, "scheduleSave: cancelling existing job for host=$host")
                job.cancel()
            }
        }
        val job = scope.launch {
            delay(saveDelayMs)
            persistHost(host)
        }
        pendingJobs[host] = job
    }

    private fun persistHost(host: String) {
        val list = memoryCache[host] ?: return
        Log.d(TAG, "persistHost: Persisting ${list.size} cookies for host: $host")
        val now = System.currentTimeMillis()
        val toSave = list.filter { it.expiresAt > now }.map { it ->
            SerializableCookie(
                name = it.name,
                value = it.value,
                expiresAt = it.expiresAt,
                domain = it.domain,
                path = it.path,
                secure = it.secure,
                httpOnly = it.httpOnly,
                hostOnly = it.hostOnly
            )
        }
        Log.d(TAG, "persistHost: Saving ${toSave.size} valid cookies to MMKV for host: $host")
        mmkv.encode(host, gson.toJson(toSave))
        pendingJobs.remove(host)
    }

    fun clear() {
        Log.d(TAG, "clear: Clearing all cookies and cancelling jobs")
        pendingJobs.values.forEach { it.cancel() }
        pendingJobs.clear()
        scope.cancel()
        memoryCache.clear()
        mmkv.clearAll()
        Log.d(TAG, "clear: Cleared MMKV and memory cache")
    }
}
