package com.dev.app.data.repository

import com.dev.app.data.datasource.order.OrderNetworkDataSource
import com.dev.app.data.local.db.OrderDao
import com.dev.app.data.local.db.OrderEntity
import com.dev.app.domain.model.Order
import com.dev.app.exception.AppException
import com.dev.app.log.AppLogger
import com.dev.app.network.Result
import com.dev.app.receiver.NetworkStateHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 订单仓库实现
 * 实现 OrderRepository 接口，负责订单数据操作
 * 支持在线/离线模式，网络优先，失败时使用本地缓存
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
  private val orderNetworkDataSource: OrderNetworkDataSource,
  private val orderDao: OrderDao
) : OrderRepository {

  companion object {
    private const val TAG = "OrderRepository"
    private const val OLD_ORDER_THRESHOLD = 24 * 60 * 60 * 1000L // 24小时
  }

  /**
   * 获取新订单列表（优先网络，失败时返回缓存）
   */
  override suspend fun getNewOrders(): Result<List<Order>> {
    return try {
      // 检查网络状态
      if (!NetworkStateHolder.isConnected) {
        AppLogger.w(TAG, "无网络连接，返回缓存数据")
        return getCachedNewOrders()
      }

      // 尝试从网络获取
      val orders = orderNetworkDataSource.getNewOrders()

      // 保存到本地缓存
      cacheOrders(orders)

      Result.Success(orders)
    } catch (e: Exception) {
      AppLogger.e(TAG, "获取新订单失败，使用缓存", e)
      getCachedNewOrders()
    }
  }

  /**
   * 获取缓存的新订单（同步方式）
   */
  private suspend fun getCachedNewOrders(): Result<List<Order>> {
    return try {
      val count = orderDao.getNewOrderCount()
      if (count > 0) {
        // TODO: 需要实现同步查询方法
        Result.Success(emptyList())
      } else {
        Result.Success(emptyList())
      }
    } catch (e: Exception) {
      AppLogger.e(TAG, "读取缓存失败", e)
      Result.Error(-1, "读取缓存失败")
    }
  }

  /**
   * 获取新订单列表（仅本地缓存）
   */
  override fun getNewOrdersFromCache(): Flow<List<Order>> {
    return orderDao.getNewOrders().map { entities ->
      entities.map { it.toDomain() }
    }
  }

  /**
   * 获取已接订单列表（仅本地缓存）
   */
  override fun getAcceptedOrdersFromCache(): Flow<List<Order>> {
    return orderDao.getAcceptedOrders().map { entities ->
      entities.map { it.toDomain() }
    }
  }

  /**
   * 获取订单详情
   */
  override suspend fun getOrderDetail(orderId: String): Result<Order> {
    return try {
      if (!NetworkStateHolder.isConnected) {
        val cached = getOrderDetailFromCache(orderId)
        if (cached != null) {
          return Result.Success(cached)
        }
        return Result.Error(-1, "无网络且无缓存")
      }

      val orders = orderNetworkDataSource.getNewOrders()
      val order = orders.find { it.id == orderId }

      if (order != null) {
        cacheOrder(order)
        Result.Success(order)
      } else {
        Result.Error(404, "订单不存在")
      }
    } catch (e: Exception) {
      val cached = getOrderDetailFromCache(orderId)
      if (cached != null) {
        Result.Success(cached)
      } else {
        Result.Error(AppException.fromThrowable(e).code, AppException.fromThrowable(e).message)
      }
    }
  }

  /**
   * 获取订单详情（仅本地缓存）
   */
  override suspend fun getOrderDetailFromCache(orderId: String): Order? {
    return orderDao.getOrderById(orderId)?.toDomain()
  }

  /**
   * 接单
   */
  override suspend fun acceptOrder(orderId: String): Result<Order> {
    return try {
      if (!NetworkStateHolder.isConnected) {
        // 离线模式：标记本地订单状态，待网络恢复后同步
        offlineAcceptOrder(orderId)
        val cached = getOrderDetailFromCache(orderId)
        return if (cached != null) {
          Result.Success(cached)
        } else {
          Result.Error(AppException.NoNetwork.code, "无网络且无缓存")
        }
      }

      val order = orderNetworkDataSource.acceptOrder(orderId)
      // 更新本地缓存
      cacheOrder(order)
      Result.Success(order)
    } catch (e: Exception) {
      // 尝试使用缓存
      val cached = getOrderDetailFromCache(orderId)
      if (cached != null) {
        offlineAcceptOrder(orderId)
        Result.Success(cached)
      } else {
        Result.Error(AppException.fromThrowable(e).code, AppException.fromThrowable(e).message)
      }
    }
  }

  /**
   * 离线接单
   */
  private suspend fun offlineAcceptOrder(orderId: String) {
    try {
      orderDao.updateOrderStatus(orderId, "ACCEPTED")
      AppLogger.d(TAG, "离线接单成功: $orderId")
    } catch (e: Exception) {
      AppLogger.e(TAG, "离线接单失败", e)
    }
  }

  /**
   * 取消订单
   */
  override suspend fun cancelOrder(orderId: String, reason: String): Result<Unit> {
    return try {
      if (!NetworkStateHolder.isConnected) {
        offlineCancelOrder(orderId, reason)
        return Result.Success(Unit)
      }

      orderNetworkDataSource.cancelOrder(orderId, reason)
      // 更新本地缓存
      orderDao.updateOrderStatus(orderId, "CANCELLED")
      Result.Success(Unit)
    } catch (e: Exception) {
      // 离线取消
      offlineCancelOrder(orderId, reason)
      Result.Success(Unit)
    }
  }

  /**
   * 离线取消订单
   */
  private suspend fun offlineCancelOrder(orderId: String, reason: String) {
    try {
      orderDao.updateOrderStatus(orderId, "CANCELLED")
      AppLogger.d(TAG, "离线取消订单成功: $orderId, 原因: $reason")
    } catch (e: Exception) {
      AppLogger.e(TAG, "离线取消订单失败", e)
    }
  }

  /**
   * 观察新订单（实时推送）
   */
  override fun observeNewOrders(): Flow<List<Order>> = flow {
    while (true) {
      try {
        if (NetworkStateHolder.isConnected) {
          val orders = orderNetworkDataSource.getNewOrders()
          cacheOrders(orders)
          emit(orders)
        } else {
          // 离线模式：从缓存获取
          orderDao.getNewOrders().collect { entities ->
            emit(entities.map { it.toDomain() })
          }
        }
      } catch (e: Exception) {
        AppLogger.e(TAG, "观察订单失败", e)
        // 发送空列表而不是抛出异常
        emit(emptyList())
      }
      delay(5000)
    }
  }

  /**
   * 同步订单数据
   */
  override suspend fun syncOrders(): Result<Unit> {
    return try {
      if (!NetworkStateHolder.isConnected) {
        return Result.Error(-1, "无网络连接")
      }

      val orders = orderNetworkDataSource.getNewOrders()
      cacheOrders(orders)
      AppLogger.d(TAG, "订单同步成功")
      Result.Success(Unit)
    } catch (e: Exception) {
      AppLogger.e(TAG, "订单同步失败", e)
      Result.Error(AppException.fromThrowable(e).code, "同步失败: ${e.message}")
    }
  }

  /**
   * 清理过期订单
   */
  override suspend fun cleanOldOrders() {
    try {
      val threshold = System.currentTimeMillis() - OLD_ORDER_THRESHOLD
      orderDao.deleteOldOrders(threshold)
      AppLogger.d(TAG, "清理过期订单完成")
    } catch (e: Exception) {
      AppLogger.e(TAG, "清理过期订单失败", e)
    }
  }

  /**
   * 缓存订单列表
   */
  private suspend fun cacheOrders(orders: List<Order>) {
    try {
      val entities = orders.map { OrderEntity.fromDomain(it) }
      orderDao.insertOrders(entities)
      AppLogger.d(TAG, "缓存订单成功: ${orders.size} 条")
    } catch (e: Exception) {
      AppLogger.e(TAG, "缓存订单失败", e)
    }
  }

  /**
   * 缓存单个订单
   */
  private suspend fun cacheOrder(order: Order) {
    try {
      orderDao.insertOrder(OrderEntity.fromDomain(order))
      AppLogger.d(TAG, "缓存订单成功: ${order.id}")
    } catch (e: Exception) {
      AppLogger.e(TAG, "缓存订单失败", e)
    }
  }
}