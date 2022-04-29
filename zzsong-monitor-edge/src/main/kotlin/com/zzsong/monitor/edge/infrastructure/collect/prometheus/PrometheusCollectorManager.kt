package com.zzsong.monitor.edge.infrastructure.collect.prometheus

import cn.idealframework.extensions.spring.RedisTemplateUtils
import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.pojo.CollectPlan
import com.zzsong.monitor.edge.application.TimeSeriesService
import com.zzsong.monitor.edge.configure.MonitorEdgeProperties
import com.zzsong.monitor.edge.infrastructure.collect.CollectorManager
import com.zzsong.monitor.edge.infrastructure.utils.timewheel.HashedWheelTimer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * @author 宋志宗 on 2022/4/24
 */
@Component
class PrometheusCollectorManager(
  private val properties: MonitorEdgeProperties,
  private val timeSeriesService: TimeSeriesService,
  private val prometheusCollector: PrometheusCollector,
  private val redisTemplate: ReactiveStringRedisTemplate,
) : CollectorManager {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusCollectorManager::class.java)
  }

  private val timeWheel =
    HashedWheelTimer(
      100,
      2048
    )

  /** 存储当前边缘集群所有的采集计划 */
  private var planMap = ConcurrentHashMap<Long, PlanWrapper>()

  override fun type() = CollectType.PROMETHEUS

  @DelicateCoroutinesApi
  override suspend fun refresh(all: List<CollectPlan>) {
    if (all.isEmpty()) {
      return
    }
    doRefreshPlan(all)
  }

  @DelicateCoroutinesApi
  override suspend fun update(update: List<CollectPlan>) {
    doUpdatePlan(update)
  }

  /**
   * 全量刷新采集计划
   *
   * @author 宋志宗 on 2022/4/23
   */
  @DelicateCoroutinesApi
  private suspend fun doRefreshPlan(all: List<CollectPlan>) {
    val planMap = ConcurrentHashMap<Long, PlanWrapper>()
    all.forEach { plan ->
      val enabled = plan.isEnabled
      if (enabled) {
        val wrapper = schedule(plan)
        planMap[plan.id] = wrapper
      }
    }
    log.info("发现prometheus采集计划 {}条", planMap.size)
    this.planMap = planMap
  }

  /**
   * 增量更新采集计划
   *
   * @author 宋志宗 on 2022/4/23
   */
  @DelicateCoroutinesApi
  private suspend fun doUpdatePlan(update: List<CollectPlan>) {
    update.forEach { plan ->
      val id = plan.id
      if (plan.isDeleted || !plan.isEnabled) {
        // 删除或者停用的都要从注册表中移除
        planMap.remove(id)
      } else {
        val wrapper = schedule(plan)
        planMap[id] = wrapper
      }
    }
    log.info("变更prometheus采集计划 {}条", update.size)
  }

  /**
   * 调度采集计划
   *
   * @author 宋志宗 on 2022/4/23
   */
  @DelicateCoroutinesApi
  private fun schedule(plan: CollectPlan): PlanWrapper {
    val planId = plan.id
    var init = false
    val wrapper = planMap.computeIfAbsent(planId) {
      init = true
      PlanWrapper(plan)
    }
    wrapper.plan = plan
    val frequency = plan.prometheus!!.frequency
    // 如果是之前不存在的采集计划, 则交给定时器进行调度
    if (init) {
      timeWheel.schedule(
        {
          GlobalScope.launch(Dispatchers.Default) {
            try {
              executeCollectPlan(planId)
            } catch (e: Exception) {
              log.info("执行采集任务出现异常: ", e)
            }
          }
        },
        frequency, TimeUnit.SECONDS
      )
    }
    return wrapper
  }

  /**
   * 执行采集计划
   *
   * @author 宋志宗 on 2022/4/23
   */
  @DelicateCoroutinesApi
  private suspend fun executeCollectPlan(planId: Long) {
    // 采集计划在等待的这段时间里可能已经被删除了, 这里做下校验
    val planWrapper = planMap[planId]
    if (planWrapper == null) {
      log.info("采集计划: {} 不存在, 可能已被删除", planId)
      return
    }
    val plan = planWrapper.plan
    val frequency = plan.prometheus!!.frequency
    // 为下次调度做准备
    timeWheel.schedule(
      {
        GlobalScope.launch(Dispatchers.Default) {
          try {
            executeCollectPlan(planId)
          } catch (e: Exception) {
            log.info("执行采集任务出现异常: ", e)
          }
        }
      },
      frequency, TimeUnit.SECONDS
    )

    try {
      // 添加分布式锁, 如果任务已经在别的节点上执行了就放弃掉
      val prefix = properties.cachePrefix
      val lockKey = "$prefix:collect_plan:lock:$planId"
      val lockValue = planWrapper.lockValue
      RedisTemplateUtils.unlock(redisTemplate, lockKey, lockValue).awaitSingleOrNull()
      val tryLock = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(frequency + 5)).awaitSingle()
      if (!tryLock) {
        log.debug("采集计划: {} 已在其他节点执行", planId)
        return
      }

      // 选择采集插件执行采集任务
      val currentTimeMillis = System.currentTimeMillis()
      planWrapper.lastExecuteTime = currentTimeMillis
      val timeSeriesList = prometheusCollector.collect(plan)
      val size = timeSeriesList.size
      if (size > 0) {
        log.debug("采集到指标数据 {}条, planId = {}", size, planId)
        timeSeriesService.saveAll(timeSeriesList)
      }
    } catch (e: Exception) {
      log.info("调度计划 {}  执行采集任务出现异常: {}", planId, e.message)
    }
  }

  /** 调度计划包装器 */
  class PlanWrapper(var plan: CollectPlan) {
    /** 分布式锁id */
    val lockValue = UUID.randomUUID().toString().replace("-", "")

    /** 上次执行时间 */
    var lastExecuteTime: Long? = null
  }
}
