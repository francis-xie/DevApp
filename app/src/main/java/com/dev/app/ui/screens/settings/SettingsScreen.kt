package com.dev.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 设置页面
 * 显示应用设置项：深色模式、通知、关于等
 * @param onNavigateBack 返回回调
 * @param viewModel 设置页 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
  val darkMode by viewModel.darkMode.collectAsState()
  val notifications by viewModel.notifications.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("设置") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        }
      )
    }
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      ListItem(
        headlineContent = { Text("深色模式") },
        trailingContent = { Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) }) }
      )
      HorizontalDivider()
      ListItem(
        headlineContent = { Text("消息通知") },
        trailingContent = { Switch(checked = notifications, onCheckedChange = { viewModel.setNotifications(it) }) }
      )
      HorizontalDivider()
      ListItem(headlineContent = { Text("关于我们") }, modifier = Modifier.clickable { })
      ListItem(headlineContent = { Text("版本") }, supportingContent = { Text("1.0.0") })
    }
  }
}