package com.example.csustdataget

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.AuthService
import com.dcelysia.csust_spider.education.ExamArrangeService
import com.dcelysia.csust_spider.mooc.data.remote.error.MoocHelperError
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        CoroutineScope(Dispatchers.IO).launch {
            AuthService.Login("202409020115","Qingyue.1026")
            val result =ExamArrangeService.getExamArrange("","期末")
            Log.d("Qingyue","${result?.get(1)?.examRoomval}")
        }
    }
}