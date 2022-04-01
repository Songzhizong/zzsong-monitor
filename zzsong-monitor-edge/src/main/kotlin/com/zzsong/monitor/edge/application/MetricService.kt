package com.zzsong.monitor.edge.application

import cn.idealframework.json.JsonUtils
import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.common.pojo.Metric
import com.zzsong.monitor.edge.configure.properties.MonitorEdgeProperties
import com.zzsong.monitor.edge.domain.model.metric.MetricPublisher
import com.zzsong.monitor.edge.domain.model.metric.MetricStore
import com.zzsong.monitor.edge.infrastructure.cache.EdgeCache
import com.zzsong.monitor.edge.infrastructure.center.CenterClient
import kotlinx.coroutines.DelicateCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author 宋志宗 on 2022/3/19
 */
@Service
class MetricService(
  private val cache: EdgeCache,
  private val metricStore: MetricStore,
  private val centerClient: CenterClient,
  private val metricPublisher: MetricPublisher,
  private val properties: MonitorEdgeProperties
) {

  companion object {
    private val log: Logger = LoggerFactory.getLogger(MetricService::class.java)
  }

  @DelicateCoroutinesApi
  suspend fun saveAll(metrics: List<Metric>) {
    //  本轮处理的所有对象标识
    val idents = HashSet<String>()
    // 对指标进行预处理并附加ident, 处理失败的指标数据将会被丢弃
    val filter = metrics.filter {
      val success = preprocess(it)
      if (success) idents.add(it.ident!!)
      success
    }
    // 将指标数据写入到存储库中
    this.writeMetrics(filter)
    // 将指标数据发布出去
    this.publishMetrics(filter)
    // 通知中心节点可能新发现了资源
    this.discoveredNotice(idents)
  }

  /** 将指标数据写入时序数据库 */
  @DelicateCoroutinesApi
  private suspend fun writeMetrics(filter: List<Metric>) {
    try {
      metricStore.write(filter)
    } catch (e: Exception) {
      log.info("持久化指标数据出现异常: ", e)
    }
  }

  /** 发布指标数据 */
  private suspend fun publishMetrics(filter: List<Metric>) {
    try {
      metricPublisher.publish(filter)
    } catch (e: Exception) {
      log.info("发布指标数据出现异常: ", e)
    }
  }

  /** 通知中心节点可能新发现了资源 */
  private fun discoveredNotice(idents: Collection<String>) {
    try {
      val filter = idents.filterTo(HashSet()) { cache.possibleNewResource(it) }
      if (filter.isEmpty()) {
        return
      }
      val clusterName = properties.clusterName
      centerClient.resourceDiscovered(clusterName, filter)
    } catch (e: Exception) {
      log.info("通知中心节点可能发现新资源出现异常: ", e)
    }
  }

  /** 对指标数据进行预处理 */
  private fun preprocess(metric: Metric): Boolean {
    val metricName = metric.metric
    // ipmi上报指标处理
    if ("ipmi_sensor_value" == metricName && !processIpmiMetric(metric)) {
      return false
    }
    // 为指标数据设置ident, 如果失败则返回false
    val supplementIdent = supplementIdent(metric)
    if (!supplementIdent) {
      return false
    }
    // 为指标数据添加对象附加标签
    val tags = cache.getTags(metric.ident!!)
    tags.forEach { (k, v) -> metric.putTag(k, v) }
    return true
  }

  /**
   * ident处理
   *
   * @author 宋志宗 on 2022/3/25
   */
  private fun supplementIdent(metric: Metric): Boolean {
    var ident = metric.ident
    // 如果ident为空, 则尝试将host设置为ident
    if (ident == null || ident.isBlank()) {
      val host = metric.removeTag(Metric.HOST_TAG)
      if (host != null && host.isNotBlank()) {
        metric.setIdent(host)
      }
    }
    ident = metric.ident
    val supplement = ident != null && ident.isNotBlank()
    if (!supplement && log.isInfoEnabled) {
      log.info("无法补充ident信息: {}", JsonUtils.toJsonString(metric))
    }
    return supplement
  }

  /**
   * 处理ipmi指标数据
   *
   * @author 宋志宗 on 2022/3/25
   */
  private fun processIpmiMetric(metric: Metric): Boolean {
    // 修改指标名称
    var name = metric.getTag("name")
    if (StringUtils.isNotBlank(name)) {
      name = if (name!!.startsWith("_")) {
        "ipmi$name"
      } else {
        "ipmi_$name"
      }
      metric.metric = name
    }
    // 如果没有ident标签, 则将其设为标签中的server
    val ident = metric.ident
    if (ident == null || ident.isBlank()) {
      val server = metric.getTag("server")
      if (server != null && server.isNotBlank()) {
        metric.setIdent(server)
      }
    }
    return true
  }
}
