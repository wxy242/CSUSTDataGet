package com.dcelysia.csust_spider.core


import android.util.Log.i
import com.dcelysia.csust_spider.core.RetrofitUtils.eduCookieJar
import com.dcelysia.csust_spider.mooc.cookie.PersistentCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitUtils {

    private const val MOOC_LOCATION = "http://pt.csust.edu.cn"
    private const val SSO_AUTH_URL = "https://authserver.csust.edu.cn"
    private const val SSO_EHALL_URL = "https://ehall.csust.edu.cn"
    private const val EDUCA_LOGIN_URL ="http://xk.csust.edu.cn"
    private val moocCookieJar by lazy { PersistentCookieJar() }
    private val eduCookieJar by lazy { PersistentCookieJar() }

    //添加公共请求头 - 用于需要认证的 API

    // MOOC 和 SSO 专用客户端 - 不包含 AuthInterceptor，添加 Cookie 支持
    private val moocClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // MOOC 系统可能较慢，增加超时时间
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor (NetworkLogger.getLoggingInterceptor())
            .cookieJar(moocCookieJar)
            .build()
    }
     val EducationClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cookieJar(eduCookieJar)
            .build()
    }

    val instanceEmptyClass : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EDUCA_LOGIN_URL)
            .client(EducationClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    }

    val instanceEduLogin: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EDUCA_LOGIN_URL)
            .client(EducationClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceMooc: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(MOOC_LOCATION)
            .client(moocClient)
            .addConverterFactory(ScalarsConverterFactory.create())  // 支持 String 响应
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceExam :Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EDUCA_LOGIN_URL)
            .client(EducationClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val instanceSSOAuth: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SSO_AUTH_URL)
            .client(moocClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instanceSSOEhall: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(SSO_EHALL_URL)
            .client(moocClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun ClearClient(client: String){
        when(client){
            "moocClient" ->{
                moocClient.cache?.evictAll()
                moocCookieJar.clear()
                moocClient.connectionPool.evictAll()
            }
            "EducationClient" ->{
                EducationClient.connectionPool.evictAll()
                EducationClient.cache?.evictAll()
                eduCookieJar.clear()
            }

        }


    }
}