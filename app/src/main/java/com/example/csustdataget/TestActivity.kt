package com.example.csustdataget

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.dcelysia.csust_spider.education.data.remote.EducationHelper
import com.dcelysia.csust_spider.education.data.remote.services.AuthService
import com.example.csustdataget.CampusCard.CampusCardHelper
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() {
    private val TAG = "TestActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lifecycleScope.launch {
            val ele = CampusCardHelper.queryElectricity("金盆岭校区","西苑11栋","324")
            Log.d(TAG, ele.toString())
            val isLogin = AuthService.Login("202408130230","@Wsl20060606")
            if (isLogin){
                Log.d(TAG,"教务登录成功")
                val response = EducationHelper.getCourseScheduleByTerm("","2025-2026-1")
                Log.d(TAG,"课表：${response[0]}")
            }
            else{
                Log.d(TAG,"教务登录失败")
            }
        }

    }
}