package com.zzsong.monitor.edge.store.prometheus

import cn.idealframework.kotlin.toJsonString
import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.edge.infrastructure.client.CenterClientImpl
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

/**
 * @author 宋志宗 on 2022/4/22
 */
@Ignore
class CenterClientImplTest {
  private val client = CenterClientImpl("http://127.0.0.1:31201")


  @Test
  fun resourceDiscovered() {
  }

  @Test
  fun findCollectPlan() = runBlocking {
    val planList = client.findCollectPlan("default")
    println(planList.toJsonString())
  }

  @Test
  fun findCollectPlan2() = runBlocking {
    val planList = client.findCollectPlan("default", CollectType.PROMETHEUS)
    println(planList.toJsonString())
  }

  @Test
  fun findRecentlyModifiedCollectPlan() = runBlocking {
    val planList = client.findRecentlyModifiedCollectPlan("default", 1650639902171)
    println(planList.toJsonString())
  }
}
