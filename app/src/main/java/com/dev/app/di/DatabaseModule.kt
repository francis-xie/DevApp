package com.dev.app.di

import android.content.Context
import com.dev.app.data.local.db.AppDatabase
import com.dev.app.data.local.db.OrderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块
 * 提供 Room 数据库和 DAO 实例
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  /**
   * 提供应用数据库实例
   */
  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return AppDatabase.getInstance(context)
  }

  /**
   * 提供订单 DAO
   */
  @Provides
  @Singleton
  fun provideOrderDao(database: AppDatabase): OrderDao {
    return database.orderDao()
  }
}
