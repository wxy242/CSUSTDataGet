package com.dcelysia.csust_spider.education.data.remote.model

data class EmptyClass(
     val stuNum: String? = null,
     val password: String? = null,
     val week: String? = null, // 查询周次
     val day: String? = null, // 查询星期几
     val term: String? = null,// 学期
     val region: String? = null ,// 校区 1云塘 2金村
     val start: String? = null, // 开始节次 从01 到 10
     val end: String? = null, // 结束周次 从01 到 10
)