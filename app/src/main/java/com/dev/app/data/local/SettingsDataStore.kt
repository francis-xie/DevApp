package com.dev.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 设置数据存储管理器
 * 负责应用设置的持久化，包括深色模式、通知等设置
 */
@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
  companion object {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val NOTIFICATIONS = booleanPreferencesKey("notifications")
  }

  /**
   * 深色模式状态流
   */
  val darkMode: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
    prefs[DARK_MODE] ?: false
  }

  /**
   * 通知状态流
   */
  val notifications: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
    prefs[NOTIFICATIONS] ?: true
  }

  /**
   * 设置深色模式
   * @param enabled 是否启用深色模式
   */
  suspend fun setDarkMode(enabled: Boolean) {
    context.settingsDataStore.edit { prefs ->
      prefs[DARK_MODE] = enabled
    }
  }

  /**
   * 设置通知
   * @param enabled 是否启用通知
   */
  suspend fun setNotifications(enabled: Boolean) {
    context.settingsDataStore.edit { prefs ->
      prefs[NOTIFICATIONS] = enabled
    }
  }
}