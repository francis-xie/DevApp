package com.dev.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token 内存缓存
 * 解决 AuthInterceptor 中 runBlocking 阻塞问题，内存缓存 Token 避免线程阻塞
 */
@Singleton
class TokenCache @Inject constructor() {

  private val _token = MutableStateFlow<String?>(null)
  val token: StateFlow<String?> = _token.asStateFlow()

  /**
   * 设置 Token 到缓存
   * @param token 要缓存的 Token
   */
  fun setToken(token: String?) {
    _token.value = token
  }

  /**
   * 从缓存获取 Token
   * @return 缓存的 Token，如果没有则返回 null
   */
  fun getToken(): String? = _token.value

  /**
   * 清除缓存的 Token
   */
  fun clear() {
    _token.value = null
  }
}