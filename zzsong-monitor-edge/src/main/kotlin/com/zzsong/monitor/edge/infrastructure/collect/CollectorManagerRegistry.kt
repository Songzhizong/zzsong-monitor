package com.zzsong.monitor.edge.infrastructure.collect

import com.zzsong.monitor.common.constants.CollectType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

/**
 * @author 宋志宗 on 2022/4/24
 */
@Component
class CollectorManagerRegistry(
  private val applicationContext: ApplicationContext
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(CollectorManagerRegistry::class.java)
  }

  private val registry = HashMap<CollectType, CollectorManager>()

  fun init() {
    val beans = applicationContext.getBeansOfType(CollectorManager::class.java)
    beans.forEach { (_, b) -> register(b) }
  }

  private fun register(collectorManager: CollectorManager) {
    val type = collectorManager.type()
    val exist = registry.put(type, collectorManager)
    if (exist != null) {
      val message = "重复注册的采集管理器: $type"
      log.error(message)
      throw RuntimeException(message)
    } else {
      log.info("成功注册采集管理器: {}", type)
    }
  }


  fun getIfPresent(collectType: CollectType): CollectorManager? {
    return registry[collectType]
  }
}
