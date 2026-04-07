package com.dev.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.dev.app.background.SyncWorker
import com.dev.app.log.AppLogger

/**
 * 引导完成接收器
 * 设备启动完成后执行初始化任务
 */
class BootCompletedReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
      AppLogger.d("BootCompletedReceiver", "设备启动完成")

      // 执行启动后的初始化任务
      initializeAfterBoot(context)
    }
  }

  private fun initializeAfterBoot(context: Context) {
    // 恢复周期性同步任务
    SyncWorker.enqueuePeriodic(context)

    AppLogger.d("BootCompletedReceiver", "启动初始化完成")
  }

  companion object {
    const val ACTION = Intent.ACTION_BOOT_COMPLETED
  }
}

/**
 * 应用更新接收器
 * 应用更新后执行初始化
 */
class AppUpdateReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      Intent.ACTION_PACKAGE_REPLACED -> {
        val packageName = intent.data?.schemeSpecificPart
        if (packageName == context.packageName) {
          AppLogger.d("AppUpdateReceiver", "应用已更新: $packageName")
          // 应用更新后的初始化逻辑
          onAppUpdated(context)
        }
      }

      Intent.ACTION_PACKAGE_INSTALL -> {
        AppLogger.d("AppUpdateReceiver", "应用已安装")
      }

      Intent.ACTION_PACKAGE_REMOVED -> {
        AppLogger.d("AppUpdateReceiver", "应用已卸载")
      }
    }
  }

  private fun onAppUpdated(context: Context) {
    // TODO: 执行应用更新后的清理或迁移工作
    // 如：迁移数据、更新数据库等
  }

  companion object {
    const val ACTION_REPLACED = "android.intent.action.PACKAGE_REPLACED"
  }
}

/**
 * 电池状态接收器
 * 监听电池电量变化
 */
class BatteryReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      Intent.ACTION_BATTERY_CHANGED -> {
        val level = intent.getIntExtra("level", -1)
        val scale = intent.getIntExtra("scale", -1)
        val percentage = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        BatteryStateHolder.level = percentage

        when (intent.getIntExtra("status", -1)) {
          android.os.BatteryManager.BATTERY_STATUS_CHARGING -> {
            BatteryStateHolder.isCharging = true
          }

          android.os.BatteryManager.BATTERY_STATUS_DISCHARGING,
          android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
            BatteryStateHolder.isCharging = false
          }
        }

        AppLogger.d("BatteryReceiver", "电量: $percentage%, 充电中: ${BatteryStateHolder.isCharging}")
      }
    }
  }

  companion object {
    fun register(context: Context) {
      val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(BatteryReceiver(), intentFilter, Context.RECEIVER_NOT_EXPORTED)
      } else {
        context.registerReceiver(BatteryReceiver(), intentFilter)
      }
    }

    fun unregister(context: Context) {
      try {
        context.unregisterReceiver(BatteryReceiver())
      } catch (e: Exception) {
        // 忽略
      }
    }
  }
}

/**
 * 电池状态持有者
 */
object BatteryStateHolder {
  var level: Int = -1
  var isCharging: Boolean = false

  fun isLow(): Boolean = level in 1..20
  fun isCritical(): Boolean = level in 1..10
}