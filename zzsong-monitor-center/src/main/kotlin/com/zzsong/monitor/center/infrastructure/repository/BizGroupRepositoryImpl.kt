package com.zzsong.monitor.center.infrastructure.repository

import com.zzsong.monitor.center.domain.model.staff.BizGroupDo
import com.zzsong.monitor.center.domain.model.staff.BizGroupRepository
import com.zzsong.monitor.center.infrastructure.MonitorIDGenerator
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * @author 宋志宗 on 2022/3/19
 */
@Repository
class BizGroupRepositoryImpl(
  private val idGenerator: MonitorIDGenerator,
  private val mongoTemplate: ReactiveMongoTemplate,
) : BizGroupRepository {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(BizGroupRepositoryImpl::class.java)
  }

  override suspend fun findById(id: Long): BizGroupDo? {
    val criteria = Criteria.where("id").`is`(id)
    val query = Query.query(criteria)
    return mongoTemplate.findOne(query, BizGroupDo::class.java).awaitSingleOrNull()
  }
}
