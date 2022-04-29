package com.zzsong.monitor.edge.infrastructure.output.rabbit

/**
 * rabbitmq时序数据写出配置
 *
 * @author 宋志宗 on 2022/4/26
 */
class RabbitOutputProperties {

  /** 交换机 */
  var exchange = ""

  /** routing key */
  var routingKey = ""
}
