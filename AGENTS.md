# DevApp 外卖接单平板应用 - 开发指南

本文档为在此代码库中工作的 AI Agent 提供指导方针。

---

## 1. 项目概述

### 1.1 基本信息

| 项目 | 说明 |
|------|------|
| **项目类型** | Android 外卖接单平板应用 |
| **技术栈** | Kotlin + Jetpack Compose |
| **最低 SDK** | 28 (Android 9.0) |
| **目标 SDK** | 35 |
| **屏幕方向** | 横屏 (landscape) |
| **屏幕常亮** | 支持 |

### 1.2 主要框架

- **依赖注入**: Hilt
- **本地数据库**: Room
- **网络请求**: Retrofit + OkHttp + Moshi
- **后台任务**: WorkManager
- **UI**: Jetpack Compose + Material 3

### 1.3 架构模式

- **Clean Architecture**: UI → Domain → Data 三层分离
- **MVVM**: ViewModel + StateFlow
- **Repository + DataSource**: 数据层双层架构

---

## 2. 构建命令

```bash
# 清理构建
./gradlew clean

# 调试构建
./gradlew assembleDebug

# 发布构建
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行 lint 分析
./gradlew lint

# 调试 APK 位置: app/build/outputs/apk/debug/
```

---

## 3. 项目结构

### 3.1 目录结构

```
com/dev/app/
├── App.kt                          # 应用入口
├── MainActivity.kt                 # 主 Activity (横屏+常亮)
│
├── data/
│   ├── local/
│   │   ├── db/                    # Room 数据库
│   │   │   ├── AppDatabase.kt     # 数据库实例
│   │   │   ├── OrderDao.kt       # 订单 DAO
│   │   │   └── OrderEntity.kt    # 订单实体
│   │   ├── SettingsDataStore.kt  # 设置存储
│   │   ├── TokenCache.kt         # Token 内存缓存
│   │   └── tokenDataStore.kt     # Token 持久化
│   │
│   ├── remote/
│   │   └── ApiService.kt         # API 接口定义
│   │
│   ├── repository/
│   │   ├── OrderRepository.kt    # 订单仓库接口
│   │   └── OrderRepositoryImpl.kt # 仓库实现 (网络优先+离线缓存)
│   │
│   └── datasource/
│       └── order/
│           ├── OrderNetworkDataSource.kt    # 数据源接口
│           └── OrderNetworkDataSourceImpl.kt # 数据源实现
│
├── domain/
│   ├── model/
│   │   ├── Order.kt              # 订单领域模型
│   │   └── OrderStatus.kt        # 订单状态枚举
│   │
│   └── usecase/
│       ├── GetOrdersUseCase.kt    # 获取订单用例
│       ├── AcceptOrderUseCase.kt  # 接单用例
│       └── CancelOrderUseCase.kt  # 取消订单用例
│
├── di/                            # Hilt 依赖注入模块
│   ├── NetworkModule.kt          # 网络模块
│   ├── RepositoryModule.kt        # 仓库模块
│   ├── DataSourceModule.kt       # 数据源模块
│   └── DatabaseModule.kt         # 数据库模块
│
├── background/                    # 后台任务 (WorkManager)
│   ├── SyncWorker.kt             # 数据同步 Worker (每6小时)
│   └── CleanupWorker.kt          # 数据清理 Worker (每天)
│
├── receiver/                       # 广播接收器
│   ├── NetworkReceiver.kt         # 网络状态监听
│   └── SystemReceiver.kt          # 系统广播 (电池/启动/更新)
│
├── network/                       # 网络相关
│   ├── Result.kt                  # 结果封装
│   ├── AuthInterceptor.kt         # 认证拦截器
│   └── RetryInterceptor.kt        # 重试拦截器
│
├── log/                           # 日志系统
│   ├── AppLogger.kt              # 日志管理器
│   ├── LogSanitizer.kt           # 日志脱敏
│   └── CrashReporter.kt          # Crash 上报
│
├── security/                      # 安全
│   └── SecureStorage.kt           # 安全存储 (EncryptedSharedPreferences)
│
├── performance/                   # 性能监控
│   └── JankStatsPerformanceMonitor.kt # 帧率监控
│
├── update/                        # 应用更新
│   └── AppUpdateManager.kt       # 应用内更新
│
├── exception/                     # 异常处理
│   └── AppException.kt           # 统一异常
│
├── ui/
│   ├── DevApp.kt                # 应用主 Composable
│   ├── DevAppState.kt           # 应用状态
│   │
│   ├── component/               # 通用组件
│   │   ├── LoadingView.kt       # 加载中
│   │   ├── ErrorView.kt         # 错误视图
│   │   └── EmptyView.kt         # 空数据
│   │
│   ├── navigation/
│   │   └── DevNavHost.kt        # 导航配置
│   │
│   ├── screens/
│   │   ├── splash/              # 启动页
│   │   │   ├── SplashScreen.kt
│   │   │   └── SplashViewModel.kt
│   │   │
│   │   ├── home/                # 接单大厅
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   │
│   │   ├── detail/              # 订单详情
│   │   │   ├── DetailScreen.kt
│   │   │   └── DetailViewModel.kt
│   │   │
│   │   └── settings/            # 设置页
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   │
│   └── theme/                   # 主题
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
│
└── util/
    └── navigation/
        └── NavigationManager.kt # 导航管理器
```

---

## 4. 应用启动流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         应用启动流程                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. MainActivity.onCreate()                                           │
│     ├── installSplashScreen() → 启动页                                │
│     ├── FLAG_KEEP_SCREEN_ON → 屏幕常亮                                  │
│     └── enableEdgeToEdge() → 边缘到边缘                                │
│                                                                         │
│  2. App.onCreate()                                                     │
│     ├── AppLogger.init() → 初始化日志                                   │
│     ├── setupExceptionHandler() → 全局异常捕获                         │
│     ├── SyncWorker.enqueuePeriodic() → 定时同步任务                    │
│     ├── CleanupWorker.enqueuePeriodic() → 定时清理任务                 │
│     └── NetworkReceiver.register() → 网络监听                          │
│                                                                         │
│  3. SplashScreen (启动页)                                              │
│     ├── 检查权限 → 请求权限                                              │
│     ├── 权限授予后调用 viewModel.startInitialization()                │
│     ├── 初始化完成后跳转主页                                           │
│                                                                         │
│  4. HomeScreen (接单大厅)                                               │
│     └── 显示订单列表，自动刷新                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 4.1 启动页流程 (正确写法)

```kotlin
// SplashViewModel.kt - 仅负责初始化状态
class SplashViewModel : ViewModel() {
    private val _splashState = MutableStateFlow<SplashState>(SplashState.Idle)
    val splashState: StateFlow<SplashState> = _splashState

    // 由 Screen 在权限授予后调用
    fun startInitialization() {
        _splashState.value = SplashState.Initializing
        viewModelScope.launch {
            delay(1000)  // 初始化
            _splashState.value = SplashState.Ready
        }
    }
}

// SplashScreen.kt - 负责权限检查和导航
@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    // 1. 检查权限
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch()
        } else {
            viewModel.startInitialization()  // 权限OK后开始初始化
        }
    }

    // 2. 初始化完成后跳转
    LaunchedEffect(splashState) {
        if (splashState is SplashState.Ready) {
            onNavigateToHome()
        }
    }
}
```

---

## 5. 代码流程

### 5.1 订单处理流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         订单处理流程                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  HomeScreen (接单大厅)                                                  │
│     │                                                                   │
│     ├── HomeViewModel.loadOrders()                                     │
│     │   │                                                               │
│     │   └── GetOrdersUseCase()                                         │
│     │       │                                                           │
│     │       └── OrderRepository.getNewOrders()                         │
│     │           │                                                       │
│     │           ├── 网络优先获取                                        │
│     │           │   └── 成功 → 缓存到 Room                            │
│     │           │                                                       │
│     │           └── 失败 → 返回 Room 缓存                              │
│     │                                                                   │
│     └── 自动刷新 (每5秒)                                                │
│         └── observeNewOrders() Flow 实时推送                           │
│                                                                         │
│  DetailScreen (订单详情)                                                │
│     │                                                                   │
│     ├── DetailViewModel.acceptOrder()                                  │
│     │   │                                                               │
│     │   └── AcceptOrderUseCase()                                        │
│     │       │                                                           │
│     │       └── OrderRepository.acceptOrder()                          │
│     │           │                                                       │
│     │           ├── 网络接单 → 更新本地缓存                            │
│     │           │                                                       │
│     │           └── 离线 → 标记本地 + 网络恢复后同步                  │
│     │                                                                   │
│     └── DetailViewModel.cancelOrder()                                  │
│         └── CancelOrderUseCase()                                        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5.2 数据流

```
网络数据 ──▶ DataSource ──▶ Repository ──▶ UseCase ──▶ ViewModel
                ↓              ↓              ↓            ↓
             模拟数据     Result<T>      Result<T>    StateFlow
                              ↓
                         Room缓存
                              ↓
                        OrderEntity
```

---

## 6. 离线支持

### 6.1 Repository 网络优先策略

```kotlin
class OrderRepositoryImpl : OrderRepository {
    // 1. 优先尝试网络
    // 2. 网络失败返回缓存
    // 3. 离线操作标记本地，网络恢复后同步
    override suspend fun getNewOrders(): Result<List<Order>> {
        return try {
            // 检查网络
            if (!NetworkStateHolder.isConnected) {
                return getCachedNewOrders()  // 离线返回缓存
            }

            // 网络获取
            val orders = orderNetworkDataSource.getNewOrders()
            cacheOrders(orders)  // 缓存到 Room
            Result.Success(orders)
        } catch (e: Exception) {
            getCachedNewOrders()  // 失败返回缓存
        }
    }
}
```

### 6.2 Room 数据库

| 表 | 说明 |
|------|------|
| `orders` | 订单缓存表 |

---

## 7. 后台任务 (WorkManager)

| Worker | 周期 | 功能 |
|--------|------|------|
| `SyncWorker` | 每 6 小时 | 同步订单数据 |
| `CleanupWorker` | 每天 | 清理过期订单(24h)+旧日志(7d) |

---

## 8. 广播接收器

| 接收器 | 作用 |
|--------|------|
| `NetworkReceiver` | 监听网络状态变化 |
| `BatteryReceiver` | 监听电池电量 |
| `BootCompletedReceiver` | 设备启动完成后恢复同步任务 |
| `AppUpdateReceiver` | 应用更新后处理 |

---

## 9. 代码规范

### 9.1 ViewModel 规范

```kotlin
// ✅ 正确：ViewModel 不处理导航，仅管理状态
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadOrders() {
        viewModelScope.launch {
            // 业务逻辑
        }
    }
}

// ❌ 错误：ViewModel 不应直接调用导航
// navigationManager.navigateToHome()  // 不要这样做
```

### 9.2 Screen 规范

```kotlin
// ✅ 正确：Screen 处理导航回调
@Composable
fun HomeScreen(onOrderClick: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Success -> {
            LazyColumn {
                items(state.orders) { order ->
                    OrderCard(onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}
```

### 9.3 UI 状态规范

```kotlin
// 使用 sealed class 定义 UI 状态
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val orders: List<Order>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```

---

## 10. Git 提交规范

```
feat: 添加新功能
fix: 修复 bug
refactor: 代码重构
style: 格式调整
chore: 构建/依赖更新
docs: 文档更新
test: 测试相关
```

---

## 11. 注意事项

- **安全**: 永远不要提交密钥、密码等敏感信息到版本库
- **架构**: ViewModel 仅管理状态，导航由 Screen 处理
- **离线**: 网络优先，失败自动降级到本地缓存
- **测试**: 新功能应包含对应的单元测试
- **兼容性**: 确保代码兼容最低 SDK 版本 (28)
