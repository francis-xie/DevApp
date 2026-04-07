package com.dev.app.network

import com.dev.app.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 认证拦截器 - 自动添加 Token
 */
class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    val token = runBlocking { tokenManager.getToken() }

    val newRequest = if (token != null) {
      originalRequest.newBuilder()
        .header("Authorization", "Bearer $token")
        .header("Content-Type", "application/json")
        .build()
    } else {
      originalRequest.newBuilder()
        .header("Content-Type", "application/json")
        .build()
    }

    return chain.proceed(newRequest)
  }
}