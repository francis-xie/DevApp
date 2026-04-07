package com.dev.app.util.navigation

import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导航管理器 - 统一管理页面跳转
 * 解决的问题：
 * 1. 避免在各处直接使用 NavController
 * 2. 统一页面跳转逻辑
 * 3. 支持参数传递
 * 4. 支持清除栈跳转到目标页面
 *
 * 单例模式的导航管理器，由 Hilt 注入
 */
@Singleton
class NavigationManager @Inject constructor() {

  /** 持有的 NavController，用于执行导航操作 */
  private var navController: NavHostController? = null

  /** 当前路由状态，用于观察当前页面 */
  private val _currentRoute = MutableStateFlow<String?>(null)
  val currentRoute: StateFlow<String?> = _currentRoute.asStateFlow()

  /**
   * 绑定 NavController
   * 在 DevApp 中调用，建立导航管理器与 NavController 的关联
   */
  fun bind(navController: NavHostController) {
    this.navController = navController
  }

  /**
   * 解除绑定
   * 在页面退出时调用，释放资源
   */
  fun unbind() {
    this.navController = null
  }

  /**
   * 通用跳转方法
   * @param route 目标路由
   * @param args 传递的参数（可选）
   */
  fun navigateTo(route: String, args: Map<String, Any>? = null) {
    navController?.let { controller ->
      // 将参数转换为查询参数格式
      val navArgs = args?.let { argsMap ->
        argsMap.entries.joinToString("/") { "${it.key}=${it.value}" }
      }
      // 拼接完整路由
      val fullRoute = if (navArgs != null) "$route?$navArgs" else route
      // 执行导航
      controller.navigate(fullRoute)
      // 更新当前路由状态
      _currentRoute.value = route
    }
  }

  /** 跳转到启动页 */
  fun navigateToSplash() = navigateTo(Route.SPLASH)

  /** 跳转到首页 */
  fun navigateToHome() = navigateTo(Route.HOME)

  /** 跳转到详情页（带 id 参数） */
  fun navigateToDetail(id: String) = navigateTo(Route.DETAIL, mapOf("id" to id))

  /** 跳转到设置页 */
  fun navigateToSettings() = navigateTo(Route.SETTINGS)

  /** 跳转到个人中心页 */
  fun navigateToProfile() = navigateTo(Route.PROFILE)

  /** 跳转到登录页 */
  fun navigateToLogin() = navigateTo(Route.LOGIN)

  /**
   * 返回上一页
   */
  fun popBack() = navController?.popBackStack()

  /**
   * 返回到指定页面
   * @param route 目标页面路由
   * @param inclusive 是否包含目标页面（true: 包含，false: 不包含）
   */
  fun popBackTo(route: String, inclusive: Boolean = false) {
    navController?.popBackStack(route, inclusive)
  }

  /**
   * 跳转到目标页面并清除栈
   * 常用于登录成功后跳转到主页，清除登录页栈
   */
  fun navigateAndClearStack(route: String) {
    navController?.let { controller ->
      controller.navigate(route) { popUpTo(0) { inclusive = true } }
      _currentRoute.value = route
    }
  }

  /**
   * 检查是否可以返回
   * @return true 表示有上一页可以返回
   */
  fun canGoBack(): Boolean = navController?.previousBackStackEntry != null
}

/**
 * 路由常量对象
 * 集中管理所有页面路由，便于维护和修改
 */
object Route {
  const val SPLASH = "splash"
  const val HOME = "home"
  const val DETAIL = "detail"
  const val SETTINGS = "settings"
  const val SEARCH = "search"
  const val PROFILE = "profile"
  const val LOGIN = "login"
}