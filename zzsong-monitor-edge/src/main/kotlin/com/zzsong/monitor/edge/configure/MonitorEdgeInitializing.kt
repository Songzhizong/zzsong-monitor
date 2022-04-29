package com.zzsong.monitor.edge.configure

import com.zzsong.monitor.edge.application.CollectPlanService
import com.zzsong.monitor.edge.infrastructure.collect.CollectorManagerRegistry
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.stereotype.Component

/**
 * @author 宋志宗 on 2022/4/24
 */
@Component
class MonitorEdgeInitializing(
  private val collectPlanService: CollectPlanService,
  private val collectorManagerRegistry: CollectorManagerRegistry
) : SmartInitializingSingleton {


  override fun afterSingletonsInstantiated() {
    collectorManagerRegistry.init()
    collectPlanService.init()
  }
}
