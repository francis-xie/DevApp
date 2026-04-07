package com.dev.app.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 启动页 ViewModel
 * 负责应用初始化
 */
@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

  private val _splashState = MutableStateFlow<SplashState>(SplashState.Idle)
  val splashState: StateFlow<SplashState> = _splashState

  /**
   * 开始初始化（在权限授予后调用）
   */
  fun startInitialization() {
    _splashState.value = SplashState.Initializing
    
    viewModelScope.launch {
      // 模拟初始化过程（可替换为真实初始化逻辑）
      // 如：加载配置、初始化 SDK、预热数据等
      kotlinx.coroutines.delay(1000)
      
      _splashState.value = SplashState.Ready
    }
  }
}

/**
 * 启动页状态
 */
sealed interface SplashState {
  /** 初始状态 */
  data object Idle : SplashState
  
  /** 初始化中 */
  data object Initializing : SplashState
  
  /** 初始化完成，可以跳转 */
  data object Ready : SplashState
}