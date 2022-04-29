package com.zzsong.monitor.edge.infrastructure.output.promethues

import cn.idealframework.lang.Lists
import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.edge.infrastructure.output.OutputChannel
import com.zzsong.monitor.edge.infrastructure.output.OutputProperties
import com.zzsong.monitor.prometheus.ReactorRemoteWriter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @author 宋志宗 on 2022/4/22
 */
@Component
class PrometheusOutputChannel(outputProperties: OutputProperties) : OutputChannel {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(PrometheusOutputChannel::class.java)
  }

  private val ready: Boolean
  private val writers = ArrayList<ReactorRemoteWriter>()

  init {
    val propertiesList = outputProperties.prometheus
    if (Lists.isNotEmpty(propertiesList)) {
      propertiesList.forEach {
        val url = it.url
        val timeout = it.timeout
        if (StringUtils.isNotBlank(url)) {
          writers.add(ReactorRemoteWriter.newInstance(url, timeout))
        }
      }
    }
    this.ready = writers.isNotEmpty()
  }

  override fun ready(): Boolean = ready

  override suspend fun output(timeSeriesList: List<TimeSeries>) {
    if (writers.isEmpty()) {
      return
    }
    coroutineScope {
      writers.map { async { it.remoteWrite(timeSeriesList).awaitSingleOrNull() } }
        .forEach {
          try {
            it.await()
          } catch (e: Exception) {
            log.info("prometheus远程写入出现异常: ", e)
          }
        }
    }
  }
}
