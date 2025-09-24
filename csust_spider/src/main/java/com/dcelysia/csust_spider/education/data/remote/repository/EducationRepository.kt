package com.dcelysia.csust_spider.education.data.remote.repository

import android.util.Log
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.CourseScheduleApi
import com.dcelysia.csust_spider.education.data.remote.model.Course
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.Exception

class EducationRepository private constructor() {
    companion object {
        val instance by lazy { EducationRepository() }
        val TAG = "EducationRepository"
    }

    private val courseScheduleApi by lazy { RetrofitUtils.instanceEduCourse.create(CourseScheduleApi::class.java) }
    
    /**
     * Gets course schedule by term and parses it into a List of Course objects
     * 
     * @param week The week number
     * @param academicSemester The academic semester identifier
     * @return List of Course objects parsed from the HTML response
     */
    suspend fun getCourseScheduleByTerm(week: String, academicSemester: String): List<Course> {
        return try {
            val response = courseScheduleApi.getCourseSchedule(week, academicSemester)
            val htmlBody = response.body().toString()
            parseCourseSchedule(htmlBody)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching course schedule", e)
            emptyList()
        }
    }
    
    /**
     * Parses HTML response into a List of Course objects
     * 
     * @param html The HTML response string from the course schedule API
     * @return List of Course objects parsed from the HTML
     */
    fun parseCourseSchedule(html: String): List<Course> {
        val courses = mutableListOf<Course>()
        
        try {
            val document: Document = Jsoup.parse(html)
            
            // 检查是否有"未查询到数据"的提示
            val emptyDataElement = document.selectFirst("td[colspan=10]")
            if (emptyDataElement != null && emptyDataElement.text().contains("未查询到数据")) {
                Log.d(TAG, "未查询到课程数据")
                return emptyList()
            }
            
            // 查找所有课程内容div
            val courseDivs = document.select("div.kbcontent")
            
            for (div in courseDivs) {
                // 检查是否有分隔符 "---------------------"
                val courseBlocks = div.html().split("---------------------")
                
                for (block in courseBlocks) {
                    // 为每个课程块创建新的Element
                    val courseBlock = Jsoup.parse(block).body()
                    
                    // 提取课程名称 - 使用第一行文本
                    val courseName = courseBlock.ownText().trim()
                    
                    var teacher: String? = null
                    var weeks: String? = null
                    var classroom: String? = null
                    var weekday: String? = null
                    
                    // 获取原始div的id并解析出星期
                    val divId = div.attr("id")
                    if (divId != null && divId.isNotEmpty()) {
                        val idParts = divId.split("-")
                        if (idParts.size >= 2) {
                            weekday = idParts[idParts.size - 2]
                        }
                    }
                    
                    // 查找当前课程块的信息
                    val fonts = courseBlock.select("font")
                    for (font in fonts) {
                        val title = font.attr("title")
                        val text = font.text()
                        
                        when (title) {
                            "老师" -> teacher = text
                            "周次(节次)" -> weeks = text
                            "教室" -> classroom = text
                        }
                    }
                    
                    // 只有当课程名不为空时才添加到列表中
                    if (courseName.isNotEmpty()) {
                        courses.add(
                            Course(
                                courseName = courseName,
                                teacher = teacher ?: "",
                                weeks = weeks ?: "",
                                classroom = classroom ?: "",
                                weekday = weekday ?: ""
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing course schedule HTML", e)
        }
        
        return courses
    }
}