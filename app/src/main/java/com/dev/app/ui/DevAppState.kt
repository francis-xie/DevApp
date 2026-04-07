package com.dev.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.dev.app.util.navigation.NavigationManager
import kotlinx.coroutines.flow.StateFlow

/**
 * 应用状态管理类
 * 持有导航控制器和导航管理器，提供全局状态访问
 * @param navController 导航控制器
 * @param navigationManager 导航管理器
 * @param isOffline 是否处于离线模式
 */
class DevAppState(
  val navController: NavHostController,
  val navigationManager: NavigationManager,
  val isOffline: Boolean
) {
  /**
   * 当前路由状态流
   */
  val currentRoute: StateFlow<String?> = navigationManager.currentRoute
}

/**
 * 创建并记住应用状态
 * 使用 remember 确保配置变更时状态保持
 * @param navController 导航控制器
 * @param navigationManager 导航管理器
 * @param isOffline 是否处于离线模式
 * @return DevAppState 应用状态实例
 */
@Composable
fun rememberDevAppState(
  navController: NavHostController,
  navigationManager: NavigationManager,
  isOffline: Boolean
): DevAppState = remember(navController, navigationManager, isOffline) {
  DevAppState(navController, navigationManager, isOffline)
}