package com.dev.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.app.domain.model.Order
import com.dev.app.domain.usecase.GetOrdersUseCase
import com.dev.app.network.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 接单大厅 ViewModel
 * 负责加载订单列表、自动刷新
 */
@HiltViewModel
class HomeViewModel @Inject constructor(private val getOrdersUseCase: GetOrdersUseCase) : ViewModel() {

  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  private var autoRefreshJob: Job? = null
  private var isAutoRefreshEnabled = true
  private val AUTO_REFRESH_INTERVAL = 5000L // 5秒

  init {
    loadOrders()
    startAutoRefresh()
  }

  fun loadOrders(refresh: Boolean = false) {
    viewModelScope.launch {
      _uiState.value = HomeUiState.Loading
      when (val result = getOrdersUseCase()) {
        is Result.Success -> {
          _uiState.value = HomeUiState.Success(result.data)
        }

        is Result.Error -> {
          _uiState.value = HomeUiState.Error(result.message)
        }

        is Result.Loading -> {
          _uiState.value = HomeUiState.Loading
        }
      }
    }
  }

  fun refresh() {
    loadOrders(refresh = true)
  }

  fun setAutoRefresh(enabled: Boolean) {
    isAutoRefreshEnabled = enabled
    if (enabled) {
      startAutoRefresh()
    } else {
      stopAutoRefresh()
    }
  }

  private fun startAutoRefresh() {
    if (!isAutoRefreshEnabled) return

    autoRefreshJob?.cancel()
    autoRefreshJob = viewModelScope.launch {
      while (isActive && isAutoRefreshEnabled) {
        delay(AUTO_REFRESH_INTERVAL)
        loadOrders()
      }
    }
  }

  private fun stopAutoRefresh() {
    autoRefreshJob?.cancel()
    autoRefreshJob = null
  }

  override fun onCleared() {
    super.onCleared()
    stopAutoRefresh()
  }
}

/**
 * 接单大厅 UI 状态
 */
sealed interface HomeUiState {
  data object Loading : HomeUiState
  data class Success(val orders: List<Order>) : HomeUiState
  data class Error(val message: String) : HomeUiState
}