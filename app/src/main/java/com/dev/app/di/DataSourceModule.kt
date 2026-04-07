package com.dev.app.di

import com.dev.app.data.datasource.order.OrderNetworkDataSource
import com.dev.app.data.datasource.order.OrderNetworkDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据源模块
 * 绑定数据源接口到实现类
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

  /**
   * 绑定订单网络数据源
   */
  @Binds
  @Singleton
  abstract fun bindOrderNetworkDataSource(impl: OrderNetworkDataSourceImpl): OrderNetworkDataSource
}