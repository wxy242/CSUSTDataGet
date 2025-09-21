package com.dcelysia.csust_spider.education.data.remote.error

sealed class EduHelperError(message: String) : Exception(message) {
    class LoginFailed(message: String) : EduHelperError(message)
    class NotLoggedIn(message: String) : EduHelperError(message)
}
