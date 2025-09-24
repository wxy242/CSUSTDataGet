package com.dcelysia.csust_spider.education

import android.util.Log
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.EmptyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern


object Empty_Service {

    private val api by lazy { RetrofitUtils.instanceEmptyClass.create(EmptyApi::class.java) }
    private val TAG = "EMPTY_CLASS"

    private val classroomNames = ArrayList<String>()


    suspend fun getClassinfo(account: String,password:String,term: String,region: String, week:String, Day:String, startLesson: String,
    endLesson: String){
        CoroutineScope(Dispatchers.IO).launch {
            val response = api.queryClassroom(
                term,
                region = region,
                startWeek = week,
                endWeek = week,
                startDay = Day,
                endDay = Day,
                startLesson = startLesson,
                endLesson = endLesson
            )

            if (response.isSuccessful) {
                val html = response.body()
                val result = Jsoup.parse(html)
                Log.d("Qingyue","${result.body()}")
                val rows = result.select("#kbtable tbody tr")
                for (row in rows) {
                    val fullText: String? = row.select("td").first().text()
                    // 使用正则表达式提取教室名称（在括号前的部分）
                    val pattern = Pattern.compile("(.*?)\\(")
                    val m: Matcher = pattern.matcher(fullText)
                    if (m.find()) {
                        var classroomName = m.group(1).trim { it <= ' ' }
                        // 去掉checkbox部分
                        classroomName = classroomName.replace("^\\s*\\S+\\s+".toRegex(), "")
                        // 添加到ArrayList
                        classroomNames.add(classroomName)
                    }
                }
                if (classroomNames.isEmpty()) {
                    Log.d(TAG,"暂无空闲教室")
                    classroomNames.add("暂无空闲教室")
                }


            }else{
                Log.d(TAG,"查询失败，${response.body()}")
            }



        }

    }
    suspend fun getEmptyClass(account: String, password: String, term: String, region: String, week: String, Day: String, startLesson: String,
                              endLesson: String): kotlin.collections.ArrayList<String>{
        CoroutineScope(Dispatchers.IO).launch {
            getClassinfo(account,password,term, region,week,Day,startLesson,endLesson)
        }
        return classroomNames
    }


}