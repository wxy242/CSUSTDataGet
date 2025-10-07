package com.example.spider_app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dcelysia.csust_spider.core.Resource
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import com.dcelysia.csust_spider.education.data.remote.services.EduCourseService
import com.dcelysia.csust_spider.mooc.data.remote.repository.MoocRepository
import com.example.csustdataget.CampusCard.CampusCardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.Authenticator

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val loginResult = MoocRepository.instance.login("202408130230","@Wsl20060606")
                    .filter { it !is Resource.Loading }
                    .first()
                    val sso_result = AuthService.Login("202408130230", "@Wsl20060606")

                    if (sso_result&&(loginResult is Resource.Success)){
                        val course = EducationHelper.getCourseScheduleByTerm("","2025-2026-1")
                        Log.d(TAG,"course:${course}")
                    }
                    else{
                        Log.d(TAG,"登陆失败")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }

            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}