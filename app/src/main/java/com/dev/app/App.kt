package com.dev.app

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.dev.app.background.CleanupWorker
import com.dev.app.background.SyncWorker
import com.dev.app.log.AppLogger
import com.dev.app.log.CrashReporter
import com.dev.app.receiver.BatteryReceiver
import com.dev.app.receiver.NetworkReceiver
import com.dev.app.update.AppUpdateManager
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口 - 初始化全局配置
 * 1. 初始化全局上下文
 * 2. 初始化日志系统
 * 3. 设置全局异常捕获
 * 4. 配置 Hilt 依赖注入
 * 5. 初始化后台任务
 * 6. 注册系统广播接收器
 */
@HiltAndroidApp
class App : Application() {

  override fun onCreate() {
    super.onCreate()

    // ========== 初始化全局上下文 ==========
    initializeContext(this)

    // ========== 初始化日志系统 ==========
    AppLogger.init(this)

    // ========== 设置全局异常捕获 ==========
    setupExceptionHandler()

    // ========== 初始化后台任务 ==========
    initializeBackgroundWork(this)

    // ========== 检查应用更新 ==========
    checkForUpdates(this)

    // ========== 注册系统广播接收器 ==========
    registerReceivers(this)

    AppLogger.d("App", "onCreate")
  }

  // ========== 全局上下文相关 ==========

  companion object {
    private lateinit var instance: Application
    private var currentActivity: Activity? = null

    /**
     * 初始化全局上下文
     */
    private fun initializeContext(app: Application) {
      instance = app
      ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }

    /**
     * 获取 Application 实例
     */
    fun getApp(): Application = instance

    /**
     * 获取 Application Context
     */
    fun getContext(): Context = instance.applicationContext

    /**
     * 获取当前活动的 Activity
     */
    fun getCurrentActivity(): Activity? = currentActivity

    /**
     * 获取指定类型的 Activity
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Activity> getCurrentActivityOfType(): T? = currentActivity as? T

    /**
     * 是否在前台
     */
    fun isForeground(): Boolean = currentActivity != null

    /**
     * 设置当前 Activity
     */
    fun setCurrentActivity(activity: Activity) {
      currentActivity = activity
    }

    private class AppLifecycleObserver : DefaultLifecycleObserver {
      override fun onStop(owner: LifecycleOwner) {
        currentActivity = null
      }
    }
  }

  // ========== 后台任务初始化 ==========

  private fun initializeBackgroundWork(context: Context) {
    // 启用周期性数据同步
    SyncWorker.enqueuePeriodic(context)

    // 启用周期性数据清理（每天执行一次）
    CleanupWorker.enqueuePeriodic(context)

    AppLogger.d("App", "Background work initialized")
  }

  // ========== 应用更新检查 ==========

  private fun checkForUpdates(context: Context) {
    AppUpdateManager.checkAndPromptUpdate(context) { apkFile ->
      // 发现更新时的回调，这里仅记录日志
      // 实际可以弹出自定义对话框让用户选择是否安装
      AppLogger.d("App", "发现更新包: ${apkFile.name}")

      // 清理旧更新包
      AppUpdateManager.cleanupOldUpdates(context)
    }
  }

  // ========== 广播接收器注册 ==========

  private fun registerReceivers(context: Context) {
    // 注册网络状态监听
    NetworkReceiver.register(context)

    // 注册电池状态监听
    BatteryReceiver.register(context)

    AppLogger.d("App", "Receivers registered")
  }

  // ========== 异常处理 ==========

  private fun setupExceptionHandler() {
    val handler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      AppLogger.e("UncaughtException", "thread: ${thread.name}", throwable)
      CrashReporter.report(throwable)
      handler?.uncaughtException(thread, throwable)
    }
  }
}