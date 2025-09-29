package com.dcelysia.csust_spider.education.data.remote.services

import android.util.Log
import com.dcelysia.csust_spider.core.RetrofitUtils
import com.dcelysia.csust_spider.education.data.remote.api.EduEhallApi
import com.dcelysia.csust_spider.education.data.remote.api.EduLoginApi
import com.dcelysia.csust_spider.education.data.remote.error.EduHelperError
import retrofit2.Response

object AuthService {

    private val Loginapi by lazy { RetrofitUtils.instanceEduLogin.create(EduLoginApi::class.java) }

    private val Ehallapi by lazy { RetrofitUtils.instanceEduLogin.create(EduEhallApi::class.java) }
    private val TAG = "AuthService"

    suspend fun CheckLoginStates(): Boolean{
        val reponse =Loginapi.checkLoginStates()
        if (isLogin(reponse)){
            throw EduHelperError.NotLoggedIn("登录失效，请重新登录")
        }else{
            return true
        }
    }

    suspend fun Login(account: String,password: String): Boolean{
        Log.d(TAG,"开始进入Login方法")
        val encode = getEhall(account,password)
        Log.d(TAG,"encode:${encode}}")
        Log.d(TAG,"getEhall方法完成")
        val reponse= Loginapi.Login(account,password,encode)
        Log.d(TAG,reponse.body().toString())
        if (isLogin(reponse)){
            Log.d(TAG,"登录失败，账号密码错误")
            throw EduHelperError.LoginFailed("用户名或密码错误！")
        }else{
            Log.d(TAG,"登录成功")
            return true
        }
    }

    private fun isLogin(reponse: Response<String>): Boolean {
        return reponse.body().toString().contains("请输入账号")
    }

    private suspend fun getEhall(account: String,password: String) : String {

        val body = Ehallapi.getEhall().body().toString()
        if (body.isEmpty()) {
            throw EduHelperError.LoginFailed("验证码响应为空")
        }
        val params = body.split("#")
        if (params.size != 2) throw EduHelperError.LoginFailed("验证码响应格式无效")

        var sourceCode = params[0]
        val sequenceHint = params[1]
        val code = "$account%%%$password"
        val encoded = StringBuilder()

        for (i in code.indices) {
            if (i < 20) {
                val charFromCode = code[i]
                val hintChar = sequenceHint[i]
                val n = hintChar.digitToIntOrNull()
                    ?: throw EduHelperError.LoginFailed("序列提示中字符无效")
                val extractedChars = sourceCode.take(n)
                encoded.append(charFromCode).append(extractedChars)
                sourceCode = sourceCode.drop(n)
            } else {
                encoded.append(code.substring(i))
                break
            }
        }
        return encoded.toString()
    }

    suspend fun LoginOut(): Boolean{
        val timeStamps = System.currentTimeMillis()
        val reponse = Loginapi.loginout(timeStamps)
        if (!reponse.isSuccessful){
            throw EduHelperError.NotLoggedIn("登出失败,${reponse.body()}")
        }else{
            RetrofitUtils.ClearClient("EducationClient")
            return true
        }



    }


}