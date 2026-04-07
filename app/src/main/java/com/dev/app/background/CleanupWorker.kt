package com.dev.app.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dev.app.data.local.db.AppDatabase
import com.dev.app.log.AppLogger
import java.util.concurrent.TimeUnit

/**
 * 数据清理 Worker
 * 用于定期清理缓存、过期数据
 */
class CleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    return try {
      AppLogger.d("CleanupWorker", "Starting cleanup...")

      // 清理过期订单（超过24小时的已完成/已取消订单）
      cleanupOldOrders()

      // 清理旧日志
      cleanupOldLogs()

      AppLogger.d("CleanupWorker", "Cleanup completed")
      Result.success()
    } catch (e: Exception) {
      AppLogger.e("CleanupWorker", "Cleanup failed", e)
      Result.failure()
    }
  }

  private suspend fun cleanupOldOrders() {
    try {
      val database = AppDatabase.getInstance(applicationContext)
      val threshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000L) // 24小时
      database.orderDao().deleteOldOrders(threshold)
      AppLogger.d("CleanupWorker", "清理过期订单完成")
    } catch (e: Exception) {
      AppLogger.e("CleanupWorker", "清理过期订单失败", e)
    }
  }

  private fun cleanupOldLogs() {
    try {
      AppLogger.get().cleanOldLogs(7) // 保留最近7天日志
      AppLogger.d("CleanupWorker", "清理旧日志完成")
    } catch (e: Exception) {
      AppLogger.e("CleanupWorker", "清理旧日志失败", e)
    }
  }

  companion object {
    private const val WORK_NAME = "cleanup_work"

    /**
     * 执行周期性清理（每天执行一次）
     */
    fun enqueuePeriodic(context: Context) {
      val workRequest = androidx.work.PeriodicWorkRequestBuilder<CleanupWorker>(
        1, TimeUnit.DAYS
      ).build()

      androidx.work.WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(WORK_NAME, androidx.work.ExistingPeriodicWorkPolicy.KEEP, workRequest)

      AppLogger.d("CleanupWorker", "Periodic cleanup enqueued")
    }

    /**
     * 取消清理任务
     */
    fun cancel(context: Context) {
      androidx.work.WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
      AppLogger.d("CleanupWorker", "Cleanup work cancelled")
    }
  }
}