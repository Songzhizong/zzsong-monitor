package com.zzsong.monitor.edge.infrastructure.output

import com.zzsong.monitor.common.pojo.TimeSeries
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

/**
 * 指标数据输出交换机
 *
 * @author 宋志宗 on 2022/4/22
 */
@Component
class OutputExchange(private val applicationContext: ApplicationContext) :
  SmartInitializingSingleton {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(OutputExchange::class.java)
  }

  private val channels = ArrayList<OutputChannel>()

  suspend fun output(timeSeriesList: List<TimeSeries>) {
    coroutineScope {
      channels.map { async { it.output(timeSeriesList) } }
        .forEach {
          try {
            it.await()
          } catch (e: Exception) {
            log.warn("异步执行数据写出抛出异常: ", e)
          }
        }
    }
  }

  override fun afterSingletonsInstantiated() {
    val beans = applicationContext.getBeansOfType(OutputChannel::class.java)
    beans.forEach { (n, c) ->
      if (c.ready()) {
        log.info("注册OutputChannel: {}", n)
        channels.add(c)
      }
    }
  }
}
