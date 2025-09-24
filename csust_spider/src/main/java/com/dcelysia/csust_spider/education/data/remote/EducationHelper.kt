package com.dcelysia.csust_spider.education.data.remote

import android.util.Log
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.CourseScheduleApi
import com.dcelysia.csust_spider.education.data.remote.model.Course
import com.dcelysia.csust_spider.education.data.remote.repository.EducationRepository

object EducationHelper {
    private val TAG = "EducationHelper"
    private val repository by lazy { EducationRepository.instance }
    
    /**
     * Gets course schedule by term and returns raw HTML string (backward compatibility)
     * 
     * @param week The week number
     * @param academicSemester The academic semester identifier
     * @return Raw HTML string of the course schedule
     */
    suspend fun getCourseScheduleByTerm(week: String, academicSemester: String): List<Course> {
        return try {
            // For backward compatibility, we still call the API but return the raw response
            // In a real implementation, you might want to store the raw response separately
            getParsedCourseScheduleByTerm(week,academicSemester)
        } catch (e: Exception) {
            Log.d(TAG,e.toString())
            emptyList()
        }
    }
    
    /**
     * Gets course schedule by term and parses it into a List of Course objects
     * 
     * @param week The week number
     * @param academicSemester The academic semester identifier
     * @return List of Course objects parsed from the course schedule
     */
    suspend fun getParsedCourseScheduleByTerm(week: String, academicSemester: String): List<Course> {
        return repository.getCourseScheduleByTerm(week, academicSemester)
    }
}