package com.dev.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.app.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页 ViewModel
 * 负责设置项的持久化
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsDataStore: SettingsDataStore) : ViewModel() {

  val darkMode: StateFlow<Boolean> = settingsDataStore.darkMode.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )

  val notifications: StateFlow<Boolean> = settingsDataStore.notifications.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), true
  )

  fun setDarkMode(enabled: Boolean) {
    viewModelScope.launch {
      settingsDataStore.setDarkMode(enabled)
    }
  }

  fun setNotifications(enabled: Boolean) {
    viewModelScope.launch {
      settingsDataStore.setNotifications(enabled)
    }
  }
}