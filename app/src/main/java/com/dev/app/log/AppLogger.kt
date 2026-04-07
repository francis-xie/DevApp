package com.dev.app.log

import android.content.Context
import android.util.Log
import com.dev.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日志管理器 - 支持 Logcat 和文件保存
 */
@Singleton
class AppLogger @Inject constructor(@ApplicationContext private val context: Context) {
  companion object {
    private const val TAG = "DevApp"
    private const val LOG_DIR = "logs"
    private const val LOG_FILE = "app.log"
    private const val MAX_LOG_SIZE = 10 * 1024 * 1024L // 10MB

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var instance: AppLogger? = null

    fun init(context: Context) {
      instance = AppLogger(context.applicationContext as android.content.Context)
    }

    fun get(): AppLogger = instance ?: throw IllegalStateException("AppLogger not initialized")

    fun d(tag: String, message: String) = get().log(Log.DEBUG, tag, message)
    fun i(tag: String, message: String) = get().log(Log.INFO, tag, message)
    fun w(tag: String, message: String, throwable: Throwable? = null) = get().log(Log.WARN, tag, message, throwable)
    fun e(tag: String, message: String, throwable: Throwable? = null) = get().log(Log.ERROR, tag, message, throwable)
  }

  private val logDir: File
    get() = File(context.filesDir, LOG_DIR).also { it.mkdirs() }

  private val logFile: File
    get() = File(logDir, LOG_FILE)

  private fun log(level: Int, tag: String, message: String, throwable: Throwable? = null) {
    val time = dateFormat.format(Date())
    val levelStr = when (level) {
      Log.DEBUG -> "D"
      Log.INFO -> "I"
      Log.WARN -> "W"
      Log.ERROR -> "E"
      else -> "V"
    }

    val logLine = buildString {
      append("$time $levelStr/$tag: $message")
      throwable?.let {
        append("\n")
        append(Log.getStackTraceString(it))
      }
    }

    // Logcat 输出
    if (BuildConfig.DEBUG) {
      when (level) {
        Log.DEBUG -> Log.d("$TAG-$tag", message)
        Log.INFO -> Log.i("$TAG-$tag", message)
        Log.WARN -> Log.w("$TAG-$tag", message, throwable)
        Log.ERROR -> Log.e("$TAG-$tag", message, throwable)
      }
    }

    // 文件输出
    writeToFile(logLine)
  }

  private fun writeToFile(content: String) {
    executor.execute {
      try {
        // 检查文件大小
        if (logFile.exists() && logFile.length() > MAX_LOG_SIZE) {
          rotateLog()
        }

        PrintWriter(FileWriter(logFile, true)).use { writer ->
          writer.println(content)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed to write log", e)
      }
    }
  }

  private fun rotateLog() {
    try {
      val timestamp = fileDateFormat.format(Date())
      val backupFile = File(logDir, "app_$timestamp.log")

      // 删除旧备份
      logDir.listFiles()?.filter {
        it.name.startsWith("app_") && it.name.endsWith(".log")
      }?.sortedByDescending { it.lastModified() }?.drop(5)?.forEach { it.delete() }

      // 重命名当前日志
      logFile.renameTo(backupFile)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to rotate log", e)
    }
  }

  /**
   * 获取日志文件内容
   */
  fun getLogContent(): String {
    return try {
      if (logFile.exists()) logFile.readText() else ""
    } catch (e: Exception) {
      "Failed to read log: ${e.message}"
    }
  }

  /**
   * 获取日志目录下的所有日志文件
   */
  fun getLogFiles(): List<File> {
    return logDir.listFiles()?.filter { it.extension == "log" }?.sortedByDescending { it.lastModified() } ?: emptyList()
  }

  /**
   * 清理旧日志（保留最近N天）
   */
  fun cleanOldLogs(daysToKeep: Int = 7) {
    executor.execute {
      val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
      logDir.listFiles()?.filter { it.lastModified() < cutoffTime }?.forEach { it.delete() }
    }
  }
}