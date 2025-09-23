package com.dcelysia.csust_spider.eduCourseGrade.data.remote.dto

data class GradeDetail(
    val components: List<GradeComponent>, //成绩组成
    val totalGrade: Int //总成绩
)