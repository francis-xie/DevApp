package com.dev.app.di

import com.dev.app.BuildConfig
import com.dev.app.data.remote.ApiService
import com.dev.app.network.AuthInterceptor
import com.dev.app.network.RetryInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络模块
 * 提供网络相关依赖：OkHttpClient、Retrofit、API 服务等
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  /**
   * 提供 OkHttpClient
   * 包含认证拦截器、重试拦截器、日志拦截器
   * Debug 模式显示完整日志，Release 模式只显示基本信息
   */
  @Provides
  @Singleton
  fun provideOkHttpClient(authInterceptor: AuthInterceptor, retryInterceptor: RetryInterceptor): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
      } else {
        HttpLoggingInterceptor.Level.BASIC
      }
    }

    return OkHttpClient.Builder()
      .addInterceptor(authInterceptor)
      .addInterceptor(retryInterceptor)
      .addInterceptor(loggingInterceptor)
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()
  }

  /**
   * 提供 Moshi 实例
   * JSON 序列化/反序列化工具
   */
  @Provides
  @Singleton
  fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

  /**
   * 提供 Retrofit 实例
   */
  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
    return Retrofit.Builder()
      .baseUrl(BuildConfig.BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
  }

  /**
   * 提供 API 服务
   */
  @Provides
  @Singleton
  fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
  }
}