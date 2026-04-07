package com.dev.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.dev.app.domain.model.Order
import com.dev.app.domain.model.OrderItem
import com.dev.app.domain.model.OrderStatus
import org.json.JSONArray
import org.json.JSONObject

/**
 * 订单实体类
 * 对应数据库中的 orders 表
 */
@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey
  val id: String,
  val orderNumber: String,
  val customerName: String,
  val customerPhone: String,
  val customerAddress: String,
  val itemsJson: String,  // Items stored as JSON string
  val totalAmount: Double,
  val status: String,
  val createdAt: Long,
  val estimatedDeliveryTime: Long?,
  val remark: String?,
  val merchantName: String,
  val merchantAddress: String?,
  val distance: Double?,
  val updatedAt: Long = System.currentTimeMillis()
) {

  /**
   * 转换为领域模型
   */
  fun toDomain(): Order {
    return Order(
      id = id,
      orderNumber = orderNumber,
      customerName = customerName,
      customerPhone = customerPhone,
      customerAddress = customerAddress,
      items = parseItems(itemsJson),
      totalAmount = totalAmount,
      status = OrderStatus.valueOf(status),
      createdAt = createdAt,
      estimatedDeliveryTime = estimatedDeliveryTime,
      remark = remark,
      merchantName = merchantName,
      merchantAddress = merchantAddress,
      distance = distance
    )
  }

  companion object {
    /**
     * 从领域模型转换为实体
     */
    fun fromDomain(order: Order): OrderEntity {
      return OrderEntity(
        id = order.id,
        orderNumber = order.orderNumber,
        customerName = order.customerName,
        customerPhone = order.customerPhone,
        customerAddress = order.customerAddress,
        itemsJson = serializeItems(order.items),
        totalAmount = order.totalAmount,
        status = order.status.name,
        createdAt = order.createdAt,
        estimatedDeliveryTime = order.estimatedDeliveryTime,
        remark = order.remark,
        merchantName = order.merchantName,
        merchantAddress = order.merchantAddress,
        distance = order.distance
      )
    }

    private fun serializeItems(items: List<OrderItem>): String {
      val jsonArray = JSONArray()
      items.forEach { item ->
        val jsonObject = JSONObject().apply {
          put("id", item.id)
          put("name", item.name)
          put("quantity", item.quantity)
          put("price", item.price)
          put("remark", item.remark)
        }
        jsonArray.put(jsonObject)
      }
      return jsonArray.toString()
    }

    private fun parseItems(json: String): List<OrderItem> {
      if (json.isEmpty()) return emptyList()
      return try {
        val jsonArray = JSONArray(json)
        (0 until jsonArray.length()).map { i ->
          val obj = jsonArray.getJSONObject(i)
          OrderItem(
            id = obj.getString("id"),
            name = obj.getString("name"),
            quantity = obj.getInt("quantity"),
            price = obj.getDouble("price"),
            remark = obj.optString("remark", null)
          )
        }
      } catch (e: Exception) {
        emptyList()
      }
    }
  }
}

/**
 * 类型转换器
 */
class Converters {

  @TypeConverter
  fun fromOrderStatus(status: OrderStatus): String = status.name

  @TypeConverter
  fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)
}
