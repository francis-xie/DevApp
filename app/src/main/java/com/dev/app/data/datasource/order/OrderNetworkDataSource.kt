package com.dev.app.data.datasource.order

import com.dev.app.domain.model.Order

/**
 * 订单网络数据源接口
 * 定义订单相关的网络数据操作
 */
interface OrderNetworkDataSource {
  /**
   * 获取新订单列表
   * @return List<Order> 订单列表
   */
  suspend fun getNewOrders(): List<Order>

  /**
   * 接单
   * @param orderId 订单 ID
   * @return Order 接单后的订单
   */
  suspend fun acceptOrder(orderId: String): Order

  /**
   * 取消订单
   * @param orderId 订单 ID
   * @param reason 取消原因
   */
  suspend fun cancelOrder(orderId: String, reason: String)
}