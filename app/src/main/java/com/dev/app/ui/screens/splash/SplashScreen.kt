package com.dev.app.ui.screens.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 启动页
 * 应用入口页面，负责权限检查和初始化导航
 * @param onNavigateToHome 导航到首页的回调
 * @param onExitApp 退出应用的回调
 * @param viewModel 启动页 ViewModel
 */
@Composable
fun SplashScreen(
  onNavigateToHome: () -> Unit,
  onExitApp: () -> Unit = {},
  viewModel: SplashViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val splashState by viewModel.splashState.collectAsState()

  var isLoading by remember { mutableStateOf(true) }
  var permissionDenied by remember { mutableStateOf(false) }

  val requiredPermissions = remember {
    buildList {
      add(Manifest.permission.INTERNET)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
      }
    }.toTypedArray()
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val allGranted = permissions.values.all { it }
    if (allGranted) {
      isLoading = false
      // 权限授予成功后，开始初始化
      viewModel.startInitialization()
    } else {
      permissionDenied = true
      isLoading = false
    }
  }

  // 首次进入时检查权限
  LaunchedEffect(Unit) {
    val allGranted = requiredPermissions.all { permission ->
      ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    if (!allGranted) {
      permissionLauncher.launch(requiredPermissions)
    } else {
      isLoading = false
      // 权限已有，开始初始化
      viewModel.startInitialization()
    }
  }

  // 监听初始化状态，完成后跳转
  LaunchedEffect(splashState) {
    if (splashState is SplashState.Ready && !permissionDenied) {
      onNavigateToHome()
    }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(text = "DevApp", style = MaterialTheme.typography.headlineLarge)

      Spacer(modifier = Modifier.height(24.dp))

      when {
        isLoading -> {
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        permissionDenied -> {
          Text(
            text = "需要必要的权限才能继续",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = {
            permissionDenied = false
            isLoading = true
            permissionLauncher.launch(requiredPermissions)
          }) { Text("重新授予权限") }
          Spacer(modifier = Modifier.height(8.dp))
          Button(onClick = onExitApp) { Text("退出应用") }
        }

        splashState is SplashState.Initializing -> {
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        else -> {
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
      }
    }
  }
}