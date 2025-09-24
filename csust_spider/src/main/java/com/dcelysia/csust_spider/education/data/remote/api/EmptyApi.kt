package com.dcelysia.csust_spider.education.data.remote.api

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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