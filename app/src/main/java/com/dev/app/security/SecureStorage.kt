package com.dev.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 安全存储管理器
 * 使用 EncryptedSharedPreferences 实现敏感数据的安全存储
 * 支持 Token 等敏感信息的加密存储
 */
@Singleton
class SecureStorage @Inject constructor(@ApplicationContext private val context: Context) {
  private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

  private val encryptedPrefs = EncryptedSharedPreferences.create(
    context, "secure_prefs", masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  /**
   * 保存 Token
   * @param token 要保存的 Token 字符串
   */
  fun saveToken(token: String) {
    encryptedPrefs.edit().putString(KEY_TOKEN, token).apply()
  }

  /**
   * 获取 Token
   * @return 存储的 Token，如果没有则返回 null
   */
  fun getToken(): String? = encryptedPrefs.getString(KEY_TOKEN, null)

  /**
   * 清除 Token
   */
  fun clearToken() {
    encryptedPrefs.edit().remove(KEY_TOKEN).apply()
  }

  companion object {
    private const val KEY_TOKEN = "auth_token"
  }
}