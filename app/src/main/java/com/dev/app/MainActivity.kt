package com.dev.app

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.dev.app.performance.JankStatsPerformanceMonitor
import com.dev.app.ui.DevApp
import com.dev.app.ui.theme.DevAppTheme
import com.dev.app.util.navigation.NavigationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 外卖接单应用主 Activity
 * - 横屏模式（平板）
 * - 屏幕常亮（接单大厅需要持续显示）
 * - 边缘到边缘
 * - 性能监控
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var navigationManager: NavigationManager

  @Inject
  lateinit var jankStatsMonitor: JankStatsPerformanceMonitor

  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)

    // ========== 屏幕常亮 ==========
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // ========== 边缘到边缘 ==========
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.auto(lightScrim = Color.TRANSPARENT, darkScrim = Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.auto(
        lightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
        darkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b),
      ),
    )

    // ========== 设置 Compose 内容 ==========
    setContent {
      DevAppTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          DevApp(navController = navController, navigationManager = navigationManager)
        }
      }
    }

    // 延迟关闭启动页
    lifecycleScope.launch {
      delay(100)
      splashScreen.setKeepOnScreenCondition { false }
    }
  }

  override fun onResume() {
    super.onResume()
    App.setCurrentActivity(this)

    // 确保屏幕常亮
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    jankStatsMonitor.initialize(this)
    jankStatsMonitor.onResume()
  }

  override fun onPause() {
    super.onPause()
    jankStatsMonitor.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    jankStatsMonitor.uninitialize()
  }
}