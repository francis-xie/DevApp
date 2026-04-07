package com.dev.app.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.app.domain.model.Order

/**
 * 订单详情页面
 * 显示订单详细信息，提供接单/取消操作
 * @param orderId 订单 ID
 * @param onNavigateBack 返回回调
 * @param viewModel 详情页 ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(orderId: String, onNavigateBack: () -> Unit, viewModel: DetailViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("订单详情") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        },
        actions = {
          TextButton(onClick = { viewModel.acceptOrder() }) {
            Text("接单")
          }
          TextButton(onClick = { viewModel.cancelOrder("") }) {
            Text("取消")
          }
        }
      )
    }
  ) { padding ->
    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
      when (val state = uiState) {
        is DetailUiState.Loading -> {
          CircularProgressIndicator()
        }

        is DetailUiState.Success -> {
          OrderDetailContent(
            orderId = orderId,
            order = state.order,
            message = state.message,
            modifier = Modifier.fillMaxSize()
          )
        }

        is DetailUiState.Error -> {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadOrder() }) {
              Text("重试")
            }
          }
        }
      }
    }
  }
}

/**
 * 订单详情内容
 */
@Composable
private fun OrderDetailContent(
  orderId: String,
  order: Order?,
  message: String,
  modifier: Modifier = Modifier
) {
  if (order == null) {
    Column(
      modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = message, style = MaterialTheme.typography.headlineSmall)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "订单 ID: $orderId",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = "暂无订单详情数据", style = MaterialTheme.typography.bodyLarge)
    }
  } else {
    Column(
      modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState())
    ) {
      Text(text = "订单号: ${order.orderNumber}", style = MaterialTheme.typography.headlineSmall)
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = "商家: ${order.merchantName}", style = MaterialTheme.typography.bodyLarge)
      Text(text = "客户: ${order.customerName}", style = MaterialTheme.typography.bodyLarge)
      Text(text = "电话: ${order.customerPhone}", style = MaterialTheme.typography.bodyLarge)
      Text(text = "地址: ${order.customerAddress}", style = MaterialTheme.typography.bodyLarge)
      Text(text = "金额: ¥${order.totalAmount}", style = MaterialTheme.typography.bodyLarge)
      order.remark?.let {
        Text(text = "备注: $it", style = MaterialTheme.typography.bodyLarge)
      }
    }
  }
}