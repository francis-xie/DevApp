package com.dev.app.data.datasource.order

import com.dev.app.data.remote.ApiService
import com.dev.app.domain.model.Order
import com.dev.app.domain.model.OrderItem
import com.dev.app.domain.model.OrderStatus
import javax.inject.Inject

/**
 * 订单网络数据源实现
 * 通过 ApiService 获取订单数据
 */
class OrderNetworkDataSourceImpl @Inject constructor(private val apiService: ApiService) : OrderNetworkDataSource {

  /**
   * 获取新订单列表
   */
  override suspend fun getNewOrders(): List<Order> {
    // TODO: 调用真实 API
    // return apiService.getNewOrders().map { it.toDomain() }

    // 模拟数据
    return generateMockOrders()
  }

  /**
   * 接单
   */
  override suspend fun acceptOrder(orderId: String): Order {
    // TODO: 调用真实 API
    return getNewOrders().first { it.id == orderId }.copy(status = OrderStatus.ACCEPTED)
  }

  /**
   * 取消订单
   */
  override suspend fun cancelOrder(orderId: String, reason: String) {
    // TODO: 调用真实 API
  }

  /**
   * 生成模拟订单数据
   */
  private fun generateMockOrders(): List<Order> {
    return listOf(
      Order(
        id = "1",
        orderNumber = "ORD20240101001",
        customerName = "张三",
        customerPhone = "13800138000",
        customerAddress = "朝阳区建国路88号SOHO现代城A座10层1001室",
        items = listOf(
          OrderItem("1", "宫保鸡丁", 1, 28.0, null),
          OrderItem("2", "米饭", 2, 3.0, null)
        ),
        totalAmount = 34.0,
        status = OrderStatus.NEW,
        createdAt = System.currentTimeMillis() - 300000,
        estimatedDeliveryTime = System.currentTimeMillis() + 2700000,
        remark = "少放辣",
        merchantName = "川菜馆",
        merchantAddress = "朝阳区建国路99号",
        distance = 1.2
      ),
      Order(
        id = "2",
        orderNumber = "ORD20240101002",
        customerName = "李四",
        customerPhone = "13900139000",
        customerAddress = "海淀区中关村大街1号",
        items = listOf(
          OrderItem("3", "汉堡套餐", 1, 35.0, null),
          OrderItem("4", "薯条", 1, 12.0, null),
          OrderItem("5", "可乐", 1, 8.0, null)
        ),
        totalAmount = 55.0,
        status = OrderStatus.NEW,
        createdAt = System.currentTimeMillis() - 120000,
        estimatedDeliveryTime = System.currentTimeMillis() + 1800000,
        remark = null,
        merchantName = "麦当劳",
        merchantAddress = "海淀区中关村大街5号",
        distance = 0.8
      ),
      Order(
        id = "3",
        orderNumber = "ORD20240101003",
        customerName = "王五",
        customerPhone = "13700137000",
        customerAddress = "东城区王府井大街138号",
        items = listOf(
          OrderItem("6", "兰州拉面", 1, 25.0, null),
          OrderItem("7", "牛肉", 1, 15.0, null)
        ),
        totalAmount = 40.0,
        status = OrderStatus.NEW,
        createdAt = System.currentTimeMillis() - 60000,
        estimatedDeliveryTime = System.currentTimeMillis() + 2400000,
        remark = "加香菜",
        merchantName = "兰州拉面",
        merchantAddress = "东城区王府井大街100号",
        distance = 2.5
      )
    )
  }
}