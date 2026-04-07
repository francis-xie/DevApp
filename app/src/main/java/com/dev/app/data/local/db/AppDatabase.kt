package com.dev.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 应用本地数据库
 * 使用 Room 存储订单等离线数据
 */
@Database(entities = [OrderEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun orderDao(): OrderDao

  companion object {
    private const val DATABASE_NAME = "devapp_database"

    @Volatile
    private var INSTANCE: AppDatabase? = null

    /**
     * 获取数据库单例
     */
    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext, AppDatabase::class.java, DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
