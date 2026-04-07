package com.dev.app.network

/**
 * 网络请求结果封装
 * 解决的问题：
 * 1. 统一成功/失败/加载中三种状态
 * 2. 避免返回 null 导致的空指针
 * 3. 方便链式调用处理结果
 *
 * sealed class Result<T> - 网络请求结果的密封类
 * - Success: 请求成功，携带数据
 * - Error: 请求失败，携带异常信息
 * - Loading: 请求进行中
 */
sealed class Result<T> {
  class Success<T>(val data: T) : Result<T>()
  class Error<T>(val code: Int, val message: String) : Result<T>()
  class Loading<T> : Result<T>()

  val isSuccess: Boolean get() = this is Success<*>
  val isError: Boolean get() = this is Error<*>
  val isLoading: Boolean get() = this is Loading<*>

  fun getOrNull(): T? = (this as? Success<*>)?.data as T?
  fun errorMessageOrNull(): String? = (this as? Error<*>)?.message

  fun <R> map(transform: (T) -> R): Result<R> = when (this) {
    is Success -> Success(transform(data))
    is Error -> Error(code, message)
    is Loading -> Loading()
  }

  inline fun onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Success) action(data)
    return this
  }

  inline fun onError(action: (Int, String) -> Unit): Result<T> {
    if (this is Error) action(code, message)
    return this
  }
}