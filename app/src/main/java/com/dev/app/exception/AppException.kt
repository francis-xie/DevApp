package com.dev.app.exception

/**
 * 统一异常处理 - 应用层异常定义
 * 解决的问题：
 * 1. 统一异常类型，便于捕获和处理
 * 2. 按业务分类（网络、认证、数据）
 * 3. 提供从 Throwable 转换的便捷方法
 *
 * sealed class AppException - 应用异常基类
 * 使用 sealed class 实现类似枚举的异常类型管理
 */
sealed class AppException(
  val code: Int = -1,  // 错误码，便于前端判断
  override val message: String  // 用户可读的错误信息
) : RuntimeException(message) {

  // ========== 网络相关异常 ==========
  /** 网络连接失败 */
  data class NetworkError(val errorCode: Int = -1, val msg: String = "网络错误") : AppException(errorCode, msg)

  /** 服务器返回错误 */
  data class ServerError(val errorCode: Int, val msg: String) : AppException(errorCode, msg)

  /** 无网络连接 */
  data object NoNetwork : AppException(-1, "无网络连接，请检查网络设置")

  /** 请求超时 */
  data object Timeout : AppException(-1, "请求超时，请稍后重试")

  /** 未知错误 */
  data object Unknown : AppException(-1, "未知错误，请稍后重试")

  // ========== 认证相关异常 ==========
  /** 登录过期或未登录 */
  data class Unauthorized(val msg: String = "登录已过期，请重新登录") : AppException(401, msg)

  /** 无权限访问 */
  data class Forbidden(val msg: String = "没有权限") : AppException(403, msg)

  /** 资源不存在 */
  data class NotFound(val msg: String = "资源不存在") : AppException(404, msg)

  /** 服务器内部错误 */
  data class ServerException(val msg: String = "服务器异常") : AppException(500, msg)

  // ========== 数据相关异常 ==========
  /** 数据库操作失败 */
  data class DatabaseError(val msg: String = "数据库错误") : AppException(-1, msg)

  /** 缓存操作失败 */
  data class CacheError(val msg: String = "缓存错误") : AppException(-1, msg)

  /**
   * 从 Throwable 转换为 AppException
   * 用于统一处理网络请求中的异常
   */
  companion object {
    fun fromThrowable(t: Throwable): AppException {
      return when (t) {
        // 如果已经是 AppException，直接返回
        is AppException -> t
        // 网络相关异常
        is java.net.UnknownHostException -> NoNetwork
        is java.net.SocketTimeoutException -> Timeout
        is java.net.ConnectException -> NoNetwork
        // Retrofit HTTP 异常
        is retrofit2.HttpException -> {
          when (t.code()) {
            401 -> Unauthorized()
            403 -> Forbidden()
            404 -> NotFound()
            in 500..599 -> ServerException()
            else -> NetworkError(t.code(), t.message ?: "网络错误")
          }
        }
        // 其他未知异常
        else -> Unknown
      }
    }
  }
}