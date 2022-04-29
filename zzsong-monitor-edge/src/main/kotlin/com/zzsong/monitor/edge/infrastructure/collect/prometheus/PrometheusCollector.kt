package com.zzsong.monitor.edge.infrastructure.collect.prometheus

import cn.idealframework.lang.Sets
import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.constants.MonitorConstants
import com.zzsong.monitor.common.pojo.CollectPlan
import com.zzsong.monitor.common.pojo.Label
import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.edge.infrastructure.collect.Collector
import com.zzsong.monitor.prometheus.ReactorExporterCollector
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * prometheus指标数据采集器
 *
 * @author 宋志宗 on 2022/4/22
 */
@Component
class PrometheusCollector : Collector {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusCollector::class.java)
    private const val jobLabelName = "job"
    private const val instanceLabelName = "instance"
  }

  private val collector = ReactorExporterCollector.newInstance(Duration.ofSeconds(5))

  override fun type() = CollectType.PROMETHEUS

  override suspend fun collect(collectPlan: CollectPlan): List<TimeSeries> {
    val planId = collectPlan.id
    val prometheus = collectPlan.prometheus
    if (prometheus == null) {
      log.error("prometheus采集配置为空, planId = {}", planId)
      return emptyList()
    }
    val exporterUrls = prometheus.exporterUrls
    if (Sets.isEmpty(exporterUrls)) {
      log.error("prometheus采集配置中exporterUrls为空, planId = {}", planId)
      return emptyList()
    }
    // 资源标识附加标签
    val ident = collectPlan.ident
    val identLabel = if (StringUtils.isBlank(ident)) {
      null
    } else {
      Label.of(MonitorConstants.IDENT, ident!!)
    }
    var hasJobLabel = false
    var hasInstanceLabel = false
    // 额外的附加标签
    val appendTags = prometheus.appendTags
    val appendLabels = appendTags?.mapNotNullTo(HashSet()) {
      val split = StringUtils.split(it, "=")
      if (split.size != 2) {
        null
      } else {
        val labelName = split[0]
        val labelValue = split[1]
        if (jobLabelName == labelName) {
          hasJobLabel = true
        }
        if (instanceLabelName == labelName) {
          hasInstanceLabel = true
        }
        Label.of(labelName, labelValue)
      }
    }
    // 将采集计划的名称作为job标签值
    val planName = collectPlan.name
    val jobLabel = if (hasJobLabel) null else Label.of("job", planName)
    // 指标名称过滤条件
    val metricPrefixes = prometheus.metricPrefixes
    val ignoreMetricPrefixes = prometheus.ignoreMetricPrefixes
    // 存储最终的返回结果
    val result = ArrayList<TimeSeries>()
    // 创建异步任务
    val timestamp = System.currentTimeMillis()
    coroutineScope {
      val asyncJobPairs =
        exporterUrls.map { Pair(it, async { collector.collect(it, timestamp).awaitSingle() }) }
      for ((url, job) in asyncJobPairs) {
        // 逐个等待异步采集任务执行完成
        val timeSeriesList = try {
          job.await()
        } catch (e: Exception) {
          log.info("采集指标数据出现异常, exporter url = {} e: {}", url, e.message)
          continue
        }
        val instanceLabel = if (hasInstanceLabel) {
          null
        } else {
          val split = StringUtils.split(url, "://", 2)
          val str = if (split.size == 1) split[0] else split[1]
          val split1 = StringUtils.split(str, "/")
          Label.of("instance", split1[0])
        }
        for (timeSeries in timeSeriesList) {
          val name = timeSeries.name
          if (name == null || name.isBlank()) {
            log.info("指标名称为空")
            continue
          }
          // 执行过滤
          var accept = true
          if (Sets.isNotEmpty(metricPrefixes)) {
            // 优先按照指定的指标前缀进行过滤, 如果符合条件则直接保留
            accept = false
            metricPrefixes!!.forEach { prefix ->
              if (StringUtils.isBlank(prefix)) {
                return@forEach
              }
              if (name.startsWith(prefix)) {
                accept = true
                return@forEach
              }
            }
          } else if (Sets.isNotEmpty(ignoreMetricPrefixes)) {
            // 如果没有指定前缀, 则根据配置的忽略前缀进行过滤, 符合条件的数据直接丢弃掉
            // 如果已经指定了需要采集的指标前缀, 则不需要执行这一步的逻辑
            ignoreMetricPrefixes!!.forEach { prefix ->
              if (name.startsWith(prefix)) {
                // 匹配上了直接丢弃掉
                accept = false
                return@forEach
              }
            }
          }
          // 过滤通过的添加额外的标签并加入返回结果集
          if (accept) {
            if (jobLabel != null) {
              timeSeries.addLabel(jobLabel)
            }
            if (instanceLabel != null) {
              timeSeries.addLabel(instanceLabel)
            }
            if (identLabel != null) {
              timeSeries.addLabel(identLabel)
            }
            if (appendLabels != null) {
              timeSeries.addLabels(appendLabels)
            }
            result.add(timeSeries)
          }
        }
      }
    }
    return result
  }
}
