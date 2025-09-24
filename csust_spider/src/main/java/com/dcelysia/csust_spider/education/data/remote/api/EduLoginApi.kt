package com.dcelysia.csust_spider.education.data.remote.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EduLoginApi {

    @GET("/jsxsd/framework/xsMain.jsp")
    suspend fun checkLoginStates(): Response<String>


    @FormUrlEncoded
    @POST("/Logon.do?method=logon")
    suspend fun Login(
        @Field("username")username: String,
        @Field("password")password:String,
        @Field("encoded")encoded: String
    ): Response<String>

    @GET("/jsxsd/xk/LoginToXk?method=exit ")
    suspend fun loginout(
        @Query("tktime") timestamp: Long
    ): Response<String>


}