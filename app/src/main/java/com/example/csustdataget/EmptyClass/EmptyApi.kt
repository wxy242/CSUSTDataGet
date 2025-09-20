package com.example.csustdataget.EmptyClass

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface EmptyApi {
    @FormUrlEncoded
    @POST("/jsxsd/kbcx/getJxlByAjax")
    suspend fun queryClassroom(
        @Field("xnxqh") term: String,           // 学期
        @Field("typewhere") typewhere: String = "jszq",
        @Field("xqbh") region: String,          // 校区
        @Field("jszt") status: String = "8",    // 教室状态
        @Field("zc") startWeek: String,         // 开始周次
        @Field("zc2") endWeek: String,          // 结束周次
        @Field("xq") startDay: String,          // 开始星期
        @Field("xq2") endDay: String,           // 结束星期
        @Field("jc") startLesson: String,       // 开始节次
        @Field("jc2") endLesson: String         // 结束节次
    ): Response<String>

}