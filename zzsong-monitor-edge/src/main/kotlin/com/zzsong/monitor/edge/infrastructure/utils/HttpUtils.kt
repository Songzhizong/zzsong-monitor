package com.zzsong.monitor.edge.infrastructure.utils

import cn.idealframework.compression.Gzip
import cn.idealframework.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xerial.snappy.Snappy

/**
 * 请求工具类
 *
 * @author 宋志宗 on 2022/4/25
 */
object HttpUtils {
  private val log: Logger = LoggerFactory.getLogger(HttpUtils::class.java)

  /**
   * 请求数据解码
   *
   * @param bytes           原始请求体
   * @param contentEncoding 编码方式
   * @author 宋志宗 on 2022/4/25
   */
  fun uncompressBody(bytes: ByteArray?, contentEncoding: String?): ByteArray? {
    if (bytes == null) {
      return null
    }
    if (StringUtils.isBlank(contentEncoding)) {
      return bytes
    }
    if ("gzip".equals(contentEncoding, ignoreCase = true)) {
      return Gzip.uncompress(bytes)
    }
    if ("snappy".equals(contentEncoding, ignoreCase = true)) {
      return Snappy.uncompress(bytes)
    }
    log.error("未知的压缩方式: {}", contentEncoding)
    return bytes
  }

}
