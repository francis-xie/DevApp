package com.dev.app.background

import android.content.Context
import androidx.work.*
import com.dev.app.log.AppLogger
import java.util.concurrent.TimeUnit

/**
 * 数据同步 Worker
 * 用于定期同步应用数据
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    return try {
      AppLogger.d("SyncWorker", "Starting sync...")
      // TODO: 执行数据同步逻辑
      // 如：同步用户信息、同步配置等
      AppLogger.d("SyncWorker", "Sync completed")
      Result.success()
    } catch (e: Exception) {
      AppLogger.e("SyncWorker", "Sync failed", e)
      if (runAttemptCount < 3) {
        Result.retry()
      } else {
        Result.failure()
      }
    }
  }

  companion object {
    private const val WORK_NAME = "data_sync"
    private const val PERIODIC_WORK_NAME = "periodic_sync"

    /**
     * 执行一次性同步
     */
    fun enqueueOneTime(context: Context) {
      val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()

      WorkManager.getInstance(context).enqueue(workRequest)
      AppLogger.d("SyncWorker", "One-time sync enqueued")
    }

    /**
     * 执行周期性同步（每6小时）
     */
    fun enqueuePeriodic(context: Context) {
      val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
        6, TimeUnit.HOURS,
        30, TimeUnit.MINUTES  // 弹性间隔
      ).setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED)
          .setRequiresBatteryNotLow(true)
          .build()
      ).build()

      WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest)
      AppLogger.d("SyncWorker", "Periodic sync enqueued")
    }

    /**
     * 取消所有同步任务
     */
    fun cancelAll(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
      AppLogger.d("SyncWorker", "All sync work cancelled")
    }
  }
}