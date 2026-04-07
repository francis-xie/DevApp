package com.dev.app.log

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash 上报（可替换为 Firebase Crashlytics）
 */
@Singleton
class CrashReporter @Inject constructor(private val context: Context) {
  companion object {
    fun report(throwable: Throwable) {
      // Firebase Crashlytics 上报
      // Firebase.crashlytics.recordException(throwable)

      // 或本地保存日志
      // 保存到本地用于下次启动上报
    }

    fun setUserId(userId: String) {
      // Firebase Crashlytics 设置用户 ID
      // Firebase.crashlytics.setUserId(userId)
    }

    fun log(message: String) {
      // Firebase Crashlytics 日志
      // Firebase.crashlytics.log(message)
    }

    fun log(message: String, tag: String = "App") {
      // 写入本地日志文件
      AppLogger.d(tag, message)
    }
  }
}