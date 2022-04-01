package com.zzsong.monitor.center.configure

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

/**
 * @author 宋志宗 on 2022/3/19
 */
@EnableReactiveMongoAuditing
@ComponentScan("com.zzsong.monitor.center")
@EntityScan("com.zzsong.monitor.center.domain.model")
@EnableReactiveMongoRepositories("com.zzsong.monitor.center")
@EnableConfigurationProperties(MonitorCenterProperties::class)
class MonitorCenterAutoConfigure {

}
