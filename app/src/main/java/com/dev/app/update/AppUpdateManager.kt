package com.dev.app.update

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.dev.app.log.AppLogger
import java.io.File

/**
 * 应用内更新管理器
 * 检测并安装应用更新包
 */
object AppUpdateManager {

  private const val APK_DIR = "updates"
  private const val TAG = "AppUpdateManager"

  /**
   * 检测并提示安装更新
   * @param context 上下文
   * @param onUpdateFound 发现更新时的回调，可自定义提示逻辑
   */
  fun checkAndPromptUpdate(
    context: Context,
    onUpdateFound: ((File) -> Unit)? = null
  ) {
    val apkFile = findUpdateApk(context)

    if (apkFile != null && apkFile.exists()) {
      AppLogger.d(TAG, "发现更新包: ${apkFile.name}")
      onUpdateFound?.invoke(apkFile)
    } else {
      AppLogger.d(TAG, "未发现更新包")
    }
  }

  /**
   * 查找更新 APK 文件
   * @return 找到的 APK 文件，如果不存在则返回 null
   */
  fun findUpdateApk(context: Context): File? {
    val updateDir = File(context.filesDir, APK_DIR)

    if (!updateDir.exists()) {
      updateDir.mkdirs()
    }

    // 查找最新的 .apk 文件
    return updateDir.listFiles()?.filter {
      it.extension.equals("apk", ignoreCase = true)
    }?.maxByOrNull { it.lastModified() }
  }

  /**
   * 提示用户安装更新
   */
  fun promptInstall(activity: Activity, onInstallComplete: (Boolean) -> Unit = {}) {
    val apkFile = findUpdateApk(activity) ?: run {
      AppLogger.w(TAG, "未找到 APK 文件")
      onInstallComplete(false)
      return
    }

    try {
      val uri = getApkUri(activity, apkFile)
      val intent = createInstallIntent(uri)

      if (activity.packageManager.resolveActivity(intent, 0) != null) {
        activity.startActivity(intent)
        AppLogger.d(TAG, "开始安装: ${apkFile.name}")
      } else {
        AppLogger.w(TAG, "无法解析安装意图")
        onInstallComplete(false)
      }
    } catch (e: Exception) {
      AppLogger.e(TAG, "安装失败", e)
      onInstallComplete(false)
    }
  }

  /**
   * 直接安装（静默检查是否有安装权限）
   * 注意：需要 SYSTEM_ALERT_WINDOW 权限才能实现真正静默安装
   */
  fun installSilently(context: Context): Boolean {
    val apkFile = findUpdateApk(context) ?: return false

    return try {
      val uri = getApkUri(context, apkFile)
      val intent = createInstallIntent(uri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      AppLogger.e(TAG, "静默安装失败", e)
      false
    }
  }

  /**
   * 将 APK 文件复制到更新目录
   * @param sourceFile 源 APK 文件
   * @return 复制后的文件
   */
  fun copyToUpdateDir(context: Context, sourceFile: File): File? {
    return try {
      val updateDir = File(context.filesDir, APK_DIR)
      if (!updateDir.exists()) updateDir.mkdirs()

      val destFile = File(updateDir, sourceFile.name)
      sourceFile.copyTo(destFile, overwrite = true)

      AppLogger.d(TAG, "APK 已复制到: ${destFile.absolutePath}")
      destFile
    } catch (e: Exception) {
      AppLogger.e(TAG, "复制 APK 失败", e)
      null
    }
  }

  /**
   * 清理旧的更新包
   * @param keepCount 保留最新的数量
   */
  fun cleanupOldUpdates(context: Context, keepCount: Int = 2) {
    val updateDir = File(context.filesDir, APK_DIR)
    if (!updateDir.exists()) return

    val apkFiles = updateDir.listFiles()?.filter {
      it.extension.equals("apk", ignoreCase = true)
    }?.sortedByDescending { it.lastModified() } ?: return

    if (apkFiles.size > keepCount) {
      apkFiles.drop(keepCount).forEach { file ->
        file.delete()
        AppLogger.d(TAG, "已删除旧更新包: ${file.name}")
      }
    }
  }

  /**
   * 获取 APK 的 Content URI
   */
  private fun getApkUri(context: Context, apkFile: File): Uri {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
    } else {
      Uri.fromFile(apkFile)
    }
  }

  /**
   * 创建安装意图
   */
  private fun createInstallIntent(uri: Uri): Intent {
    return Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(uri, "application/vnd.android.package-archive")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }
}