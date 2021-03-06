package com.zzsong.monitor.center.application

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author 宋志宗 on 2022/3/26
 */
@Service
@Transactional(rollbackFor = [Throwable::class])
class MetricService {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(MetricService::class.java)
  }


}
