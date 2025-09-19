package com.example.csustdataget.core

import com.example.changli_planet_app.feature.mooc.cookie.PersistentCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitUtils {
    private const val FreshNewsIp = "http://113.44.47.220:8085/app/"
    private const val UserIp = "http://113.44.47.220:8083/app/users/"
    private const val IpLocation ="http://ip-api.com/json/"
    private const val MOOC_LOCATION = "http://pt.csust.edu.cn"
    private const val SSO_AUTH_URL = "https://authserver.csust.edu.cn"
    private const val SSO_EHALL_URL = "https://ehall.csust.edu.cn"
    private const val EMPTY_CLASS_URL =""

    //添加公共请求头 - 用于需要认证的 API

    // MOOC 和 SSO 专用客户端 - 不包含 AuthInterceptor，添加 Cookie 支持
    private val moocClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // MOOC 系统可能较慢，增加超时时间
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor (NetworkLogger.getLoggingInterceptor())
            .cookieJar(PersistentCookieJar())
            .build()
    }
    private val EmptyClassClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val instanceEmptyClass : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EMPTY_CLASS_URL)
            .client(EmptyClassClient)
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
}