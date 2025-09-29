package com.example.spider_app

import android.app.Application
import com.tencent.mmkv.MMKV

class CSUSTApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化 MMKV，返回根目录路径（可选）
        val rootDir = MMKV.initialize(this)
        // 可选打印用于确认
        println("MMKV initialized at: $rootDir")
    }
}