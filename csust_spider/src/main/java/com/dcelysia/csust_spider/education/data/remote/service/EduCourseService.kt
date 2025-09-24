package com.dcelysia.csust_spider.education.data.remote.service

import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.EduCourseApi
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import com.dcelysia.csust_spider.education.data.remote.model.CourseGrade
import com.dcelysia.csust_spider.education.data.remote.model.CourseNature
import com.dcelysia.csust_spider.education.data.remote.model.DisplayMode
import com.dcelysia.csust_spider.education.data.remote.model.GradeComponent
import com.dcelysia.csust_spider.education.data.remote.model.GradeDetail
import com.dcelysia.csust_spider.education.data.remote.model.StudyMode
import org.jsoup.Jsoup

object EduCourseService {

    private val courseApi by lazy {
        RetrofitUtils.instanceScoreInquiry.create(EduCourseApi::class.java)
    }

    private val TAG = "EDU_COURSE"

    suspend fun getCourseGrades(
        academicYearSemester: String? = null,
        courseNature: CourseNature? = null,
        courseName: String = "",
        displayMode: DisplayMode = DisplayMode.BEST_GRADE,
        studyMode: StudyMode = StudyMode.MAJOR
    ): List<CourseGrade> {

        AuthService.CheckLoginStates()

        val response = courseApi.getCourseGrades(
            academicYearSemester ?: "", //学年学期，格式为 "2023-2024-1"，如果为 `nil` 则为全部学期
            courseNature?.id ?: "", //课程性质，如果为 `nil` 则查询所有性质的课程
            courseName, //课程名称，默认为空字符串表示查询所有课程
            displayMode.id, //显示模式，默认为显示最好成绩
            studyMode.id //修读方式，默认为主修
        )

        if (!response.isSuccessful) {
            throw EduHelperError.CourseGradesRetrievalFailed("网络请求失败：${response.code()}")
        }

        val html = response.body() ?: throw EduHelperError.CourseGradesRetrievalFailed("响应体为空")
        return parseCourseGrades(html)
    }

    suspend fun getAvailableSemestersForCourseGrades(): List<String> {

        AuthService.CheckLoginStates()

        val response = courseApi.getCourseGradePage()

        if(!response.isSuccessful) {
            throw EduHelperError.AvailableSemestersForCourseGradesRetrievalFailed("网络请求失败：${response.code()}")
        }

        val html = response.body() ?: throw EduHelperError.AvailableSemestersForCourseGradesRetrievalFailed("响应体为空")
        return parseAvailableSemesters(html)
    }

    suspend fun getGradeDetail(url: String): GradeDetail {

        AuthService.CheckLoginStates()
        // 学校返回数据有误 手动修改
        val fixedUrl = url.replace(",com",".cn")

        val response = courseApi.getGradeDetail(fixedUrl)

        if (!response.isSuccessful) {
            throw EduHelperError.GradeDetailRetrievalFailed("网络请求失败：${response.code()}")
        }

        val html = response.body() ?: throw EduHelperError.GradeDetailRetrievalFailed("响应体为空")
        return parseGradeDetail(html)
    }

    private fun parseCourseGrades(html: String): List<CourseGrade> { //可以加withContext(Dispatchers.IO)包裹耗时的Jsoup解析-属于CPU密集型
        val document = Jsoup.parse(html)
        val table = document.selectFirst("#dataList")
            ?: throw EduHelperError.CourseGradesRetrievalFailed("未找到课程成绩表格")

        if (table.text().contains("未查询到数据")) return emptyList()

        val rows = table.select("tr")
        val courseGrades = mutableListOf<CourseGrade>()

        for (i in 1 until rows.size) {
            val row = rows[i]
            val cols = row.select("td")

            if (cols.size < 17) {
                throw EduHelperError.CourseGradesRetrievalFailed("行列数不足：${cols.size}")
            }

            try{
                val semester = cols[1].text().trim()
                val courseID = cols[2].text().trim()
                val courseName = cols[3].text().trim()
                val groupName = cols[4].text().trim()
                val gradeString = cols[5].text().trim()
                val grade = gradeString.toIntOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("成绩格式无效：$gradeString")
                val gradeDetailUrlElement = cols[5].select("a")
                var gradeDetailUrl = gradeDetailUrlElement?.attr("href")?.trim() ?: ""

                gradeDetailUrl = gradeDetailUrl
                    .replace("javascript:openWindow('","http://xk.csust.edu,com")
                    .replace("',700,500)","")

                val studyMode = cols[6].text().trim()
                val gradeIdentifier = cols[7].text().trim()
                val credit = cols[8].text().trim().toDoubleOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("学分格式无效: ${cols[8].text()}")

                val totalHours = cols[9].text().trim().toIntOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("总学时格式无效: ${cols[9].text()}")

                val gradePoint = cols[10].text().trim().toDoubleOrNull()
                    ?: throw EduHelperError.CourseGradesRetrievalFailed("绩点格式无效: ${cols[10].text()}")

                val retakeSemester = cols[11].text().trim()
                val assessmentMethod = cols[12].text().trim()
                val examNature = cols[13].text().trim()
                val courseAttribute = cols[14].text().trim()

                val courseNatureString = cols[15].text().trim()
                val courseNature = CourseNature.Companion.fromChineseName(courseNatureString)

                val courseCategory = cols[16].text().trim()

                val courseGrade = CourseGrade(
                    semester, courseID, courseName, groupName, grade, gradeDetailUrl,
                    studyMode, gradeIdentifier, credit, totalHours, gradePoint, retakeSemester,
                    assessmentMethod, examNature, courseAttribute, courseNature, courseCategory
                )

                courseGrades.add(courseGrade)
            } catch (e: Exception) {
                if (e is EduHelperError) throw e
                throw EduHelperError.CourseGradesRetrievalFailed("解析成绩时出错：${e.message}")
            }
        }

        return courseGrades
    }

    private fun parseAvailableSemesters(html: String): List<String> {
        val document = Jsoup.parse(html)
        val semesterSelect = document.selectFirst("#kksj")
            ?: throw EduHelperError.AvailableSemestersForCourseGradesRetrievalFailed("未找到学期选择元素")

        val options = semesterSelect.select("option")
        val semesters = mutableListOf<String>()

        for (option in options) {
            val name = option.text().trim()
            if (name.contains("全部学期")) continue
            semesters.add(name)
        }

        return semesters
    }

    private fun parseGradeDetail(html: String): GradeDetail {
        val document = Jsoup.parse(html)
        val table = document.selectFirst("#dataList")
            ?: throw EduHelperError.GradeDetailRetrievalFailed("未找到成绩详情表格")

        val rows = table.select("tr")
        if (rows.size < 2) {
            throw EduHelperError.GradeDetailRetrievalFailed("成绩详情表行数不足")
        }

        val headerRow = rows[0]
        val headerCols = headerRow.select("th")
        val valueRow = rows[1]
        val valueCols = valueRow.select("td")

        if (headerCols.size < 4 || valueCols.size < 4) {
            throw EduHelperError.GradeDetailRetrievalFailed("成绩详情表列数不足")
        }

        val components = mutableListOf<GradeComponent>()

        for (i in 1 until headerCols.size - 1 step 2) {
            val type = headerCols[i].text().trim()
            val gradeString = valueCols[i].text().trim()
            val ratioString = valueCols[i + 1].text().trim().replace("%", "")

            val grade = gradeString.toDoubleOrNull()
                ?: throw EduHelperError.GradeDetailRetrievalFailed("成绩格式无效: $gradeString")

            val ratio = ratioString.toIntOrNull()
                ?: throw EduHelperError.GradeDetailRetrievalFailed("比例格式无效: $ratioString")

            components.add(GradeComponent(type, grade, ratio))
        }

        val totalGradeString = valueCols.last()?.text()?.trim()
        val totalGrade = totalGradeString?.toIntOrNull()
            ?: throw EduHelperError.GradeDetailRetrievalFailed("总成绩格式无效: $totalGradeString")

        return GradeDetail(components, totalGrade)
    }
}