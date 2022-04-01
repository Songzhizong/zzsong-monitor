package com.zzsong.monitor.center.domain.model.staff

import cn.idealframework.transmission.exception.ResourceNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/3/19
 */
@Repository
class BizGroupRepository {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(BizGroupRepository::class.java)
  }

  suspend fun findById(id: Long): BizGroupDo? {

    return null
  }

  suspend fun findByRequiredId(id: Long): BizGroupDo {
    return findById(id) ?: kotlin.run {
      log.info("业务组: {} 不存在", id)
      throw ResourceNotFoundException("业务组不存在")
    }
  }
}
