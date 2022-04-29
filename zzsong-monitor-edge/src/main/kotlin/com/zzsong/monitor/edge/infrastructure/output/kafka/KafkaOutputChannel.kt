package com.zzsong.monitor.edge.infrastructure.output.kafka

import cn.idealframework.json.JsonUtils
import cn.idealframework.lang.Lists
import cn.idealframework.lang.StringUtils
import com.zzsong.monitor.common.pojo.TimeSeries
import com.zzsong.monitor.edge.infrastructure.output.OutputChannel
import com.zzsong.monitor.edge.infrastructure.output.OutputProperties
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord

/**
 * @author 宋志宗 on 2022/4/26
 */
@Component
class KafkaOutputChannel(outputProperties: OutputProperties) : OutputChannel {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(KafkaOutputChannel::class.java)
  }

  private val ready: Boolean
  private val senders = ArrayList<KafkaSenderWrapper>()

  init {
    val propertiesList = outputProperties.kafka
    if (Lists.isNotEmpty(propertiesList)) {
      propertiesList.forEach {
        val brokers = it.brokers
        val topic = it.topic
        if (StringUtils.isAnyBlank(brokers, topic)) {
          log.info("kafka output brokers 或者 topic 配置为空")
          return@forEach
        }
        val properties = HashMap<String, Any>()
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = brokers
        properties[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        properties[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        val options = SenderOptions.create<String, String>(properties)
        val sender = KafkaSender.create(options)
        val wrapper = KafkaSenderWrapper(sender, topic, brokers)
        senders.add(wrapper)
      }
    }
    this.ready = senders.isNotEmpty()
  }

  override fun ready(): Boolean = ready

  override suspend fun output(timeSeriesList: List<TimeSeries>) {
    if (senders.isEmpty()) {
      return
    }
    coroutineScope {
      senders.map { wrapper ->
        async {
          val sender = wrapper.sender
          val topic = wrapper.topic
          val records = timeSeriesList.map { ts ->
            val value = JsonUtils.toJsonString(ts)
            SenderRecord.create(topic, null, null, ts.name, value, null)
          }
          try {
            sender.send(Flux.fromIterable(records)).collectList().awaitSingleOrNull()
          } catch (e: Exception) {
            val brokers = wrapper.brokers
            log.info("发布消息到卡夫卡出现异常, brokers = {} , topic = {} , e: ", brokers, topic, e)
          }
          true
        }
      }.forEach {
        try {
          it.await()
        } catch (e: Exception) {
          log.info("kafka发布数据出现异常: ", e)
        }
      }
    }
  }

  class KafkaSenderWrapper(
    val sender: KafkaSender<String, String>,
    val topic: String,
    val brokers: String
  ) {

  }
}
