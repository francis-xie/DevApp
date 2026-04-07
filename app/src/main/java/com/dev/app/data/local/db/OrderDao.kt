package com.dev.app.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 订单数据访问接口
 * 定义订单数据的本地数据库操作
 */
@Dao
interface OrderDao {

  /**
   * 获取所有订单
   */
  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun getAllOrders(): Flow<List<OrderEntity>>

  /**
   * 获取新订单（未接单）
   */
  @Query("SELECT * FROM orders WHERE status = 'NEW' ORDER BY createdAt DESC")
  fun getNewOrders(): Flow<List<OrderEntity>>

  /**
   * 获取已接订单
   */
  @Query("SELECT * FROM orders WHERE status != 'NEW' ORDER BY createdAt DESC")
  fun getAcceptedOrders(): Flow<List<OrderEntity>>

  /**
   * 根据 ID 获取订单
   */
  @Query("SELECT * FROM orders WHERE id = :orderId")
  suspend fun getOrderById(orderId: String): OrderEntity?

  /**
   * 插入订单（冲突时替换）
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity)

  /**
   * 批量插入订单
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrders(orders: List<OrderEntity>)

  /**
   * 更新订单
   */
  @Update
  suspend fun updateOrder(order: OrderEntity)

  /**
   * 更新订单状态
   */
  @Query("UPDATE orders SET status = :status WHERE id = :orderId")
  suspend fun updateOrderStatus(orderId: String, status: String)

  /**
   * 删除订单
   */
  @Query("DELETE FROM orders WHERE id = :orderId")
  suspend fun deleteOrder(orderId: String)

  /**
   * 删除所有订单
   */
  @Query("DELETE FROM orders")
  suspend fun deleteAllOrders()

  /**
   * 清理过期订单（超过 24 小时的已完成/已取消订单）
   */
  @Query("DELETE FROM orders WHERE status IN ('COMPLETED', 'CANCELLED') AND createdAt < :timestamp")
  suspend fun deleteOldOrders(timestamp: Long)

  /**
   * 获取订单数量
   */
  @Query("SELECT COUNT(*) FROM orders")
  suspend fun getOrderCount(): Int

  /**
   * 获取新订单数量
   */
  @Query("SELECT COUNT(*) FROM orders WHERE status = 'NEW'")
  suspend fun getNewOrderCount(): Int
}
