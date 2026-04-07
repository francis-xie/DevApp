package com.dev.app.data.repository

import com.dev.app.domain.model.Order
import com.dev.app.network.Result
import kotlinx.coroutines.flow.Flow

/**
 * 订单仓库接口
 * 定义订单相关的业务操作
 * 支持在线和离线模式
 */
interface OrderRepository {
  /**
   * 获取新订单列表（优先网络，失败时返回缓存）
   * @return Result<List<Order>> 订单列表结果
   */
  suspend fun getNewOrders(): Result<List<Order>>

  /**
   * 获取新订单列表（仅本地缓存）
   * @return 缓存的订单列表
   */
  fun getNewOrdersFromCache(): Flow<List<Order>>

  /**
   * 获取已接订单列表（仅本地缓存）
   * @return 缓存的已接订单列表
   */
  fun getAcceptedOrdersFromCache(): Flow<List<Order>>

  /**
   * 获取订单详情（优先网络）
   * @param orderId 订单 ID
   * @return 订单详情
   */
  suspend fun getOrderDetail(orderId: String): Result<Order>

  /**
   * 获取订单详情（仅本地缓存）
   * @param orderId 订单 ID
   * @return 缓存的订单详情
   */
  suspend fun getOrderDetailFromCache(orderId: String): Order?

  /**
   * 接单
   * @param orderId 订单 ID
   * @return Result<Order> 接单结果
   */
  suspend fun acceptOrder(orderId: String): Result<Order>

  /**
   * 取消订单
   * @param orderId 订单 ID
   * @param reason 取消原因
   * @return Result<Unit> 取消结果
   */
  suspend fun cancelOrder(orderId: String, reason: String): Result<Unit>

  /**
   * 观察新订单（Flow 实时推送）
   * @return Flow<List<Order>> 订单流
   */
  fun observeNewOrders(): Flow<List<Order>>

  /**
   * 同步订单数据（网络到本地）
   * 用于手动触发同步
   */
  suspend fun syncOrders(): Result<Unit>

  /**
   * 清理过期订单数据
   */
  suspend fun cleanOldOrders()
}