package com.example.csustdataget

import android.app.Application
import com.tencent.mmkv.MMKV

class CSUSTDataGetApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}