package com.dev.app.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.app.domain.model.Order
import com.dev.app.domain.usecase.AcceptOrderUseCase
import com.dev.app.domain.usecase.CancelOrderUseCase
import com.dev.app.network.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 详情页 ViewModel
 * 负责加载订单详情、处理接单/取消操作
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val acceptOrderUseCase: AcceptOrderUseCase,
  private val cancelOrderUseCase: CancelOrderUseCase
) : ViewModel() {

  private val orderId: String = savedStateHandle.get<String>("id") ?: ""

  private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
  val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

  init {
    loadOrder()
  }

  /**
   * 加载订单详情
   */
  fun loadOrder() {
    _uiState.value = DetailUiState.Loading
    // TODO: 实际应调用获取订单详情的 UseCase
    // 暂时显示模拟数据
    _uiState.value = DetailUiState.Success(
      order = null,
      message = "订单详情"
    )
  }

  /**
   * 接单
   */
  fun acceptOrder() {
    viewModelScope.launch {
      _uiState.value = DetailUiState.Loading
      when (val result = acceptOrderUseCase(orderId)) {
        is Result.Success -> {
          _uiState.value = DetailUiState.Success(order = result.data, message = "接单成功")
        }

        is Result.Error -> {
          _uiState.value = DetailUiState.Error(result.message)
        }

        is Result.Loading -> {}
      }
    }
  }

  /**
   * 取消订单
   */
  fun cancelOrder(reason: String) {
    viewModelScope.launch {
      _uiState.value = DetailUiState.Loading
      when (val result = cancelOrderUseCase(orderId, reason)) {
        is Result.Success -> {
          _uiState.value = DetailUiState.Success(order = null, message = "取消成功")
        }

        is Result.Error -> {
          _uiState.value = DetailUiState.Error(result.message)
        }

        is Result.Loading -> {}
      }
    }
  }
}

/**
 * 详情页 UI 状态
 */
sealed interface DetailUiState {
  data object Loading : DetailUiState
  data class Success(val order: Order?, val message: String) : DetailUiState
  data class Error(val message: String) : DetailUiState
}