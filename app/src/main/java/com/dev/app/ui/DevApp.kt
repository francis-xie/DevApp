package com.dev.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.dev.app.ui.navigation.DevNavHost
import com.dev.app.util.navigation.NavigationManager

/**
 * 应用主 Composable
 * 1. 绑定导航控制器
 * 2. 创建应用状态
 * 3. 设置导航主机
 *
 * 应用主入口 Composable
 */
@Composable
fun DevApp(navController: NavHostController, navigationManager: NavigationManager, isOffline: Boolean = false) {
  LaunchedEffect(navController) {
    navigationManager.bind(navController)
  }

  val appState = rememberDevAppState(
    navController = navController,
    navigationManager = navigationManager,
    isOffline = isOffline
  )

  DevNavHost(appState = appState)
}