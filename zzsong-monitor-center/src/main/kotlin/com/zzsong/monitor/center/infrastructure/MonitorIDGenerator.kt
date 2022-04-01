package com.zzsong.monitor.center.infrastructure

import cn.idealframework.id.IDGenerator
import cn.idealframework.id.IDGeneratorFactory
import org.springframework.stereotype.Component

/**
 * @author 宋志宗 on 2022/3/19
 */
@Component
class MonitorIDGenerator(idGeneratorFactory: IDGeneratorFactory) : IDGenerator {
  private val idGenerator = idGeneratorFactory.getGenerator("zzsong_monitor_center")

  override fun generate(): Long {
    return idGenerator.generate()
  }

}
