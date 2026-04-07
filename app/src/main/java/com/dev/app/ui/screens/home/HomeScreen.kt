package com.dev.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dev.app.domain.model.Order
import com.dev.app.domain.model.OrderStatus

/**
 * 接单大厅首页
 * 平板横屏显示，持续刷新新订单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOrderClick: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Store, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("接单大厅", fontWeight = FontWeight.Bold)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        actions = {
          var autoRefresh by remember { mutableStateOf(true) }
          TextButton(onClick = {
            autoRefresh = !autoRefresh
            viewModel.setAutoRefresh(autoRefresh)
          }) {
            Icon(
              if (autoRefresh) Icons.Default.Sync else Icons.Default.SyncDisabled,
              contentDescription = null
            )
            Spacer(Modifier.width(4.dp))
            Text(if (autoRefresh) "自动刷新" else "已暂停")
          }
          IconButton(onClick = { viewModel.refresh() }) {
            Icon(Icons.Default.Refresh, contentDescription = "刷新")
          }
        }
      )
    }
  ) { padding ->
    when (val state = uiState) {
      is HomeUiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is HomeUiState.Success -> {
        if (state.orders.isEmpty()) {
          EmptyOrderHall(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
          OrderList(
            orders = state.orders,
            onOrderClick = onOrderClick,
            modifier = Modifier.fillMaxSize().padding(padding)
          )
        }
      }

      is HomeUiState.Error -> {
        com.dev.app.ui.component.ErrorView(
          message = state.message,
          onRetry = { viewModel.refresh() },
          modifier = Modifier.fillMaxSize().padding(padding)
        )
      }
    }
  }
}

/**
 * 订单列表
 */
@Composable
private fun OrderList(
  orders: List<Order>,
  onOrderClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    items(orders, key = { it.id }) { order ->
      OrderCard(order = order, onClick = { onOrderClick(order.id) })
    }
  }
}

/**
 * 订单卡片
 */
@Composable
private fun OrderCard(
  order: Order,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "订单号: ${order.orderNumber}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        OrderStatusChip(status = order.status)
      }

      Spacer(Modifier.height(12.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(order.merchantName, style = MaterialTheme.typography.bodyLarge)
      }

      Spacer(Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(order.customerName, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(order.customerPhone, style = MaterialTheme.typography.bodyMedium)
      }

      Spacer(Modifier.height(8.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.width(8.dp))
        Text(
          text = order.customerAddress,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      HorizontalDivider(Modifier.padding(vertical = 12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${order.items.size} 件商品",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "¥${String.format("%.2f", order.totalAmount)}",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      order.remark?.let { remark ->
        if (remark.isNotEmpty()) {
          Spacer(Modifier.height(8.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.Info,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
              "备注: $remark",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.tertiary
            )
          }
        }
      }
    }
  }
}

/**
 * 订单状态标签
 */
@Composable
private fun OrderStatusChip(status: OrderStatus) {
  val (color, text) = when (status) {
    OrderStatus.NEW -> Color(0xFF4CAF50) to "新订单"
    OrderStatus.ACCEPTED -> Color(0xFF2196F3) to "已接单"
    OrderStatus.PREPARING -> Color(0xFFFF9800) to "准备中"
    OrderStatus.PICKED_UP -> Color(0xFF9C27B0) to "已取餐"
    OrderStatus.DELIVERING -> Color(0xFF00BCD4) to "配送中"
    OrderStatus.COMPLETED -> Color(0xFF607D8B) to "已完成"
    OrderStatus.CANCELLED -> Color(0xFFF44336) to "已取消"
  }

  Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.1f)) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium
    )
  }
}

/**
 * 空接单大厅
 */
@Composable
private fun EmptyOrderHall(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      Icons.Default.Inbox,
      contentDescription = null,
      modifier = Modifier.size(80.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))
    Text("暂无新订单", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}