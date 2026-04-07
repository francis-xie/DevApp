package com.dev.app.log

import android.util.Log

/**
 * 日志脱敏工具
 * 防止敏感信息泄露到日志
 */
object LogSanitizer {

  /**
   * 需要脱敏的字段
   */
  private val sensitiveFields = setOf(
    "password", "pwd", "passwd",
    "token", "access_token", "refresh_token", "jwt",
    "phone", "mobile", "tel",
    "idCard", "id_card", "cardNo", "card_no",
    "bankCard", "bank_card", "bankNo",
    "cvv", "cvc", "expireDate",
    "email", "mail",
    "address", "addr", "location",
    "name", "realName", "real_name",
    "session", "sessionId", "session_id",
    "cookie", "sessionKey",
    "apiKey", "api_key", "secret", "appSecret",
    "authorization", "auth"
  )

  /**
   * 脱敏手机号
   */
  fun sanitizePhone(phone: String): String {
    return if (phone.length >= 11) {
      "${phone.substring(0, 3)}****${phone.substring(7)}"
    } else {
      "****"
    }
  }

  /**
   * 脱敏邮箱
   */
  fun sanitizeEmail(email: String): String {
    val atIndex = email.indexOf('@')
    return if (atIndex > 0) {
      val prefix = email.substring(0, atIndex)
      if (prefix.length > 2) {
        "${prefix.substring(0, 2)}***${email.substring(atIndex)}"
      } else {
        "***${email.substring(atIndex)}"
      }
    } else {
      "***"
    }
  }

  /**
   * 脱敏身份证号
   */
  fun sanitizeIdCard(idCard: String): String {
    return if (idCard.length >= 18) {
      "${idCard.substring(0, 6)}********${idCard.substring(14)}"
    } else if (idCard.length >= 15) {
      "${idCard.substring(0, 6)}****${idCard.substring(12)}"
    } else {
      "****"
    }
  }

  /**
   * 脱敏银行卡号
   */
  fun sanitizeBankCard(cardNo: String): String {
    return if (cardNo.length >= 16) {
      "${cardNo.substring(0, 6)}****${cardNo.substring(cardNo.length - 4)}"
    } else {
      "****"
    }
  }

  /**
   * 脱敏 Token
   */
  fun sanitizeToken(token: String): String {
    return if (token.length > 20) {
      "${token.substring(0, 10)}...${token.substring(token.length - 10)}"
    } else {
      "***"
    }
  }

  /**
   * 脱敏 JSON 对象中的敏感字段
   */
  fun sanitizeJson(json: String): String {
    var result = json

    // 替换手机号格式 (可能有各种格式)
    val phoneRegex = """(1[3-9]\d{9})""".toRegex()
    result = phoneRegex.replace(result) { sanitizePhone(it.value) }

    // 替换邮箱格式
    val emailRegex = """[\w.-]+@[\w.-]+\.\w+""".toRegex()
    result = emailRegex.replace(result) { sanitizeEmail(it.value) }

    // 替换身份证号 (15位或18位)
    val idCardRegex = """[1-9]\d{5}((19|20)\d{2})(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]""".toRegex()
    result = idCardRegex.replace(result) { sanitizeIdCard(it.value) }

    return result
  }

  /**
   * 脱敏 URL 中的敏感参数
   */
  fun sanitizeUrl(url: String): String {
    var result = url

    // 替换 URL 中的敏感参数
    val sensitiveParams = listOf(
      "password", "pwd", "token", "access_token",
      "session_id", "api_key", "sign"
    )

    sensitiveParams.forEach { param ->
      val regex = """($param=)[^&]*""".toRegex(RegexOption.IGNORE_CASE)
      result = regex.replace(result, "$1***")
    }

    return result
  }

  /**
   * 脱敏通用方法 - 检测并脱敏任何包含敏感字段的字符串
   */
  fun sanitize(message: String): String {
    var result = message

    // 检测是否为 JSON
    if (result.trim().startsWith("{") || result.trim().startsWith("[")) {
      result = sanitizeJson(result)
    }

    // 检测是否为 URL
    if (result.contains("http://") || result.contains("https://")) {
      result = sanitizeUrl(result)
    }

    // 检测手机号
    if (result.contains(Regex("1[3-9]\\d{9}"))) {
      result = result.replace(Regex("1[3-9]\\d{9}")) {
        sanitizePhone(it.value)
      }
    }

    // 检测邮箱
    if (result.contains(Regex("""[\w.-]+@[\w.-]+\.\w+"""))) {
      result = result.replace(Regex("""[\w.-]+@[\w.-]+\.\w+""")) {
        sanitizeEmail(it.value)
      }
    }

    return result
  }
}

/**
 * 安全日志扩展
 */
fun safeLog(tag: String, message: String, level: Int = Log.INFO) {
  val sanitized = LogSanitizer.sanitize(message)
  when (level) {
    Log.DEBUG -> AppLogger.d(tag, sanitized)
    Log.INFO -> AppLogger.i(tag, sanitized)
    Log.WARN -> AppLogger.w(tag, sanitized)
    Log.ERROR -> AppLogger.e(tag, sanitized)
  }
}

/**
 * 安全日志 - Debug
 */
fun safeD(tag: String, message: String) = safeLog(tag, message, Log.DEBUG)

/**
 * 安全日志 - Info
 */
fun safeI(tag: String, message: String) = safeLog(tag, message, Log.INFO)

/**
 * 安全日志 - Error
 */
fun safeE(tag: String, message: String, throwable: Throwable? = null) {
  val sanitized = LogSanitizer.sanitize(message)
  AppLogger.e(tag, sanitized, throwable)
}