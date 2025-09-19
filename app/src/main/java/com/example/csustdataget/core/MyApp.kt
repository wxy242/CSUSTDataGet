package com.example.csustdataget.core

import android.app.Application
import android.util.Log
import com.tencent.mmkv.MMKV

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val rootDir = MMKV.initialize(this)
        Log.d("App", "MMKV initialized: $rootDir")
    }
}