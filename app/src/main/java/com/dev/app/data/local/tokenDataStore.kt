package com.dev.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_prefs")

/**
 * Token 管理器
 * 负责 Token 的持久化和内存缓存，支持登录状态管理
 */
@Singleton
class TokenManager @Inject constructor(
  @ApplicationContext private val context: Context, private val tokenCache: TokenCache
) {
  companion object {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val EXPIRES_AT = longPreferencesKey("expires_at")
  }

  /**
   * 保存 Token 到持久化存储和内存缓存
   * @param accessToken 访问令牌
   * @param refreshToken 刷新令牌
   * @param expiresIn 过期时间（秒）
   */
  suspend fun saveToken(accessToken: String, refreshToken: String, expiresIn: Long) {
    context.tokenDataStore.edit { prefs ->
      prefs[ACCESS_TOKEN] = accessToken
      prefs[REFRESH_TOKEN] = refreshToken
      prefs[EXPIRES_AT] = System.currentTimeMillis() + (expiresIn * 1000)
    }
    tokenCache.setToken(accessToken)
  }

  /**
   * 获取 Token，优先从内存缓存获取
   * @return Token 字符串，如果没有则返回 null
   */
  suspend fun getToken(): String? {
    return tokenCache.getToken() ?: context.tokenDataStore.data.first()[ACCESS_TOKEN]
  }

  /**
   * 获取刷新 Token
   * @return 刷新 Token
   */
  suspend fun getRefreshToken(): String? {
    return context.tokenDataStore.data.first()[REFRESH_TOKEN]
  }

  /**
   * 检查 Token 是否过期
   * @return true 表示已过期，false 表示未过期
   */
  suspend fun isTokenExpired(): Boolean {
    val expiresAt = context.tokenDataStore.data.first()[EXPIRES_AT] ?: return true
    return System.currentTimeMillis() >= expiresAt
  }

  /**
   * 清除 Token（同时清除持久化和内存缓存）
   */
  suspend fun clearToken() {
    context.tokenDataStore.edit { it.clear() }
    tokenCache.clear()
  }

  /**
   * 观察登录状态
   * @return Flow<Boolean> 登录状态流
   */
  fun isLoggedIn(): Flow<Boolean> = context.tokenDataStore.data.map { prefs ->
    prefs[ACCESS_TOKEN] != null
  }
}