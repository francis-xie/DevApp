package com.dev.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dev.app.ui.DevAppState
import com.dev.app.ui.screens.detail.DetailScreen
import com.dev.app.ui.screens.home.HomeScreen
import com.dev.app.ui.screens.settings.SettingsScreen
import com.dev.app.ui.screens.splash.SplashScreen
import com.dev.app.util.navigation.Route

/**
 * 应用导航配置
 * 平板横屏外卖接单应用导航
 */
@Composable
fun DevNavHost(appState: DevAppState) {
  val navController = appState.navController

  NavHost(navController = navController, startDestination = Route.SPLASH) {
    composable(Route.SPLASH) {
      SplashScreen(
        onNavigateToHome = { appState.navigationManager.navigateAndClearStack(Route.HOME) },
        onExitApp = { android.os.Process.killProcess(android.os.Process.myPid()) }
      )
    }

    // 接单大厅首页
    composable(Route.HOME) {
      HomeScreen(onOrderClick = { orderId -> appState.navigationManager.navigateToDetail(orderId) })
    }

    // 订单详情
    composable(
      route = "${Route.DETAIL}/{id}",
      arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { backStackEntry ->
      val orderId = backStackEntry.arguments?.getString("id") ?: ""
      DetailScreen(orderId = orderId, onNavigateBack = { appState.navigationManager.popBack() })
    }

    // 设置
    composable(Route.SETTINGS) {
      SettingsScreen(onNavigateBack = { appState.navigationManager.popBack() })
    }
  }
}