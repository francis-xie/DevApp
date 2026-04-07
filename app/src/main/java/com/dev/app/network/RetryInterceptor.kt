package com.dev.app.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * 重试拦截器
 * 网络请求自动重试，处理服务器错误情况
 */
class RetryInterceptor @Inject constructor() : Interceptor {

  companion object {
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L
  }

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    var response: Response? = null
    var exception: IOException? = null

    for (attempt in 1..MAX_RETRIES) {
      try {
        response?.close()
        response = chain.proceed(request)

        if (response.isSuccessful || response.code < 500) {
          return response
        }

        response.close()
        if (attempt < MAX_RETRIES) {
          Thread.sleep(RETRY_DELAY_MS * attempt)
        }
      } catch (e: IOException) {
        exception = e
        if (attempt < MAX_RETRIES) {
          Thread.sleep(RETRY_DELAY_MS * attempt)
        }
      }
    }

    throw exception ?: IOException("Request failed after $MAX_RETRIES retries")
  }
}