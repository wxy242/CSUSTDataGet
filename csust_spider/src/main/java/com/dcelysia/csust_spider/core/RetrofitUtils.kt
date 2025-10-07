package com.dcelysia.csust_spider.core


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

    private const val CAMPUS_CARD_LOCATION = "http://yktwd.csust.edu.cn:8988/"
    private val totalCookieJar by lazy { PersistentCookieJar.instance }
    private val moocClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // MOOC 系统可能较慢，增加超时时间
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor (NetworkLogger.getLoggingInterceptor())
            .cookieJar(totalCookieJar)
            .build()
    }
     val EducationClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor (NetworkLogger.getLoggingInterceptor() )
            .cookieJar(totalCookieJar)
            .build()
    }
    val campusClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cookieJar(totalCookieJar)
            .build()
    }

    val instanceScoreInquiry : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(EDUCA_LOGIN_URL)
            .client(EducationClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
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
    val instanceEduCourse: Retrofit by lazy {
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
    val instanceCampus : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(CAMPUS_CARD_LOCATION)
            .client(campusClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun ClearClient(client: String){
        when(client){
            "moocClient" ->{
                moocClient.cache?.evictAll()
                totalCookieJar.clear()
                moocClient.connectionPool.evictAll()
            }
            "EducationClient" ->{
                EducationClient.connectionPool.evictAll()
                EducationClient.cache?.evictAll()
                totalCookieJar.clear()
            }

        }


    }
}