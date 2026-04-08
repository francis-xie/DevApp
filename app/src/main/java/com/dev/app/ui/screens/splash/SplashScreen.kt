package com.dev.app.ui.screens.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * 启动页
 * 应用入口页面，负责权限检查和初始化导航
 */
@Composable
fun SplashScreen(
  onNavigateToHome: () -> Unit,
  onExitApp: () -> Unit = {},
  viewModel: SplashViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val activity = context as? ComponentActivity
  val lifecycleOwner = LocalLifecycleOwner.current
  val splashState by viewModel.splashState.collectAsState()

  // 状态: 0=检查中, 1=已授权可初始化, 2=被拒绝可重试, 3=永久拒绝
  var permissionStatus by remember { mutableIntStateOf(0) }

  val requiredPermissions = remember {
    buildList {
      add(Manifest.permission.INTERNET)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
      }
    }.toTypedArray()
  }

  // 检查权限状态
  fun checkPermissions(): Boolean {
    return requiredPermissions.all { permission ->
      ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
  }

  // 请求权限
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val allGranted = permissions.values.all { it }
    if (allGranted) {
      permissionStatus = 1  // 已授权
      viewModel.startInitialization()
    } else {
      // 检查是否还能再次请求
      val canRetry = requiredPermissions.any { permission ->
        activity?.shouldShowRequestPermissionRationale(permission) == true
      }
      permissionStatus = if (canRetry) 2 else 3
    }
  }

  // 首次进入检查权限
  LaunchedEffect(Unit) {
    if (checkPermissions()) {
      permissionStatus = 1
      viewModel.startInitialization()
    } else {
      // 直接请求权限
      permissionLauncher.launch(requiredPermissions)
    }
  }

  // 监听应用从后台回到前台
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        if (checkPermissions() && permissionStatus != 1) {
          permissionStatus = 1
          viewModel.startInitialization()
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // 初始化完成后跳转
  LaunchedEffect(splashState) {
    if (splashState is SplashState.Ready && permissionStatus == 1) {
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

      when (permissionStatus) {
        0 -> {
          // 检查中
          CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        1 -> {
          // 已授权
          if (splashState is SplashState.Initializing) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "初始化中...", style = MaterialTheme.typography.bodyMedium)
          } else {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
          }
        }

        2 -> {
          // 被拒绝，可重试
          Text(
            text = "需要必要的权限才能继续",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = {
            permissionStatus = 0
            permissionLauncher.launch(requiredPermissions)
          }) { Text("重新授予权限") }
          Spacer(modifier = Modifier.height(8.dp))
          Button(onClick = onExitApp) {
            Text("退出应用")
          }
        }

        3 -> {
          // 永久拒绝
          Text(
            text = "权限被拒绝，请到设置开启",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = {
            val intent = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            context.startActivity(android.content.Intent(intent).apply {
              data = android.net.Uri.parse("package:${context.packageName}")
            })
          }) { Text("去设置") }
          Spacer(modifier = Modifier.height(8.dp))
          Button(onClick = onExitApp) { Text("退出应用") }
        }
      }
    }
  }
}