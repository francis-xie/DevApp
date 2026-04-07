package com.dev.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.dev.app.log.AppLogger

/**
 * 网络状态监听器
 * 监听网络连接状态变化
 */
class NetworkReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val connectionType = when {
      capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
      capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "移动数据"
      capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "以太网"
      else -> "无网络"
    }

    AppLogger.d("NetworkReceiver", "网络状态: ${if (isConnected) "已连接 ($connectionType)" else "未连接"}")

    // 发送网络状态变化事件
    NetworkStateHolder.isConnected = isConnected
    NetworkStateHolder.connectionType = connectionType
  }

  companion object {
    /**
     * 注册网络监听
     */
    fun register(context: Context) {
      val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(NetworkReceiver(), intentFilter, Context.RECEIVER_NOT_EXPORTED)
      } else {
        context.registerReceiver(NetworkReceiver(), intentFilter)
      }
      AppLogger.d("NetworkReceiver", "注册网络监听")
    }

    /**
     * 取消注册
     */
    fun unregister(context: Context) {
      try {
        context.unregisterReceiver(NetworkReceiver())
        AppLogger.d("NetworkReceiver", "取消注册网络监听")
      } catch (e: Exception) {
        // 忽略未注册异常
      }
    }
  }
}

/**
 * 网络状态持有者
 * 提供全局网络状态访问
 */
object NetworkStateHolder {
  var isConnected: Boolean = false
  var connectionType: String = "无网络"

  fun isWifi(): Boolean = connectionType == "WiFi"
  fun isMobileData(): Boolean = connectionType == "移动数据"
}