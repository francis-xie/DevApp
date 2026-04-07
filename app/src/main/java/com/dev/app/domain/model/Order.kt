package com.dev.app.domain.model

/**
 * 订单状态
 */
enum class OrderStatus {
  NEW,           // 新订单
  ACCEPTED,      // 已接单
  PREPARING,     // 准备中
  PICKED_UP,     // 已取餐
  DELIVERING,    // 配送中
  COMPLETED,     // 已完成
  CANCELLED      // 已取消
}

/**
 * 订单领域模型
 */
data class Order(
  val id: String,
  val orderNumber: String,
  val customerName: String,
  val customerPhone: String,
  val customerAddress: String,
  val items: List<OrderItem>,
  val totalAmount: Double,
  val status: OrderStatus,
  val createdAt: Long,
  val estimatedDeliveryTime: Long?,
  val remark: String?,
  val merchantName: String,
  val merchantAddress: String?,
  val distance: Double?
)

/**
 * 订单项
 */
data class OrderItem(
  val id: String,
  val name: String,
  val quantity: Int,
  val price: Double,
  val remark: String?
)

/**
 * 骑手配送订单
 */
data class DeliveryOrder(
  val order: Order,
  val deliveryFee: Double,
  val deliveryAddress: String,
  val customerLatitude: Double,
  val customerLongitude: Double
)

/**
 * 接单统计
 */
data class OrderStats(
  val todayOrders: Int,
  val todayRevenue: Double,
  val pendingOrders: Int,
  val completedOrders: Int,
  val averageRating: Float
)