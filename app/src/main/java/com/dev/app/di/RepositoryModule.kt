package com.dev.app.di

import com.dev.app.data.repository.OrderRepository
import com.dev.app.data.repository.OrderRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 仓库模块
 * 绑定仓库接口到实现类
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  /**
   * 绑定订单仓库
   */
  @Binds
  @Singleton
  abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}