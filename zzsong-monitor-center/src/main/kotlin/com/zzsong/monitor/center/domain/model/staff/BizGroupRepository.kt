package com.zzsong.monitor.center.domain.model.staff

import cn.idealframework.transmission.exception.ResourceNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author 宋志宗 on 2022/3/19
 */
interface BizGroupRepository {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(BizGroupRepository::class.java)
  }

  suspend fun findById(id: Long): BizGroupDo?

  suspend fun findRequiredById(id: Long): BizGroupDo {
    return findById(id) ?: kotlin.run {
      log.info("业务组: {} 不存在", id)
      throw ResourceNotFoundException("业务组不存在")
    }
  }
}
