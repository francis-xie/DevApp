package com.dev.app.domain.usecase

import com.dev.app.data.repository.OrderRepository
import com.dev.app.domain.model.Order
import com.dev.app.network.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取接单大厅订单列表用例
 */
class GetOrdersUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  suspend operator fun invoke(): Result<List<Order>> {
    return orderRepository.getNewOrders()
  }
}

/**
 * 获取缓存订单列表用例（离线模式）
 */
class GetCachedOrdersUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  operator fun invoke(): Flow<List<Order>> {
    return orderRepository.getNewOrdersFromCache()
  }
}

/**
 * 获取已接订单列表用例（离线模式）
 */
class GetAcceptedOrdersUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  operator fun invoke(): Flow<List<Order>> {
    return orderRepository.getAcceptedOrdersFromCache()
  }
}

/**
 * 获取订单详情用例
 */
class GetOrderDetailUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  suspend operator fun invoke(orderId: String): Result<Order> {
    return orderRepository.getOrderDetail(orderId)
  }
}

/**
 * 接单用例
 */
class AcceptOrderUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  suspend operator fun invoke(orderId: String): Result<Order> {
    return orderRepository.acceptOrder(orderId)
  }
}

/**
 * 取消订单用例
 */
class CancelOrderUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  suspend operator fun invoke(orderId: String, reason: String): Result<Unit> {
    return orderRepository.cancelOrder(orderId, reason)
  }
}

/**
 * 同步订单数据用例
 */
class SyncOrdersUseCase @Inject constructor(private val orderRepository: OrderRepository) {
  suspend operator fun invoke(): Result<Unit> {
    return orderRepository.syncOrders()
  }
}