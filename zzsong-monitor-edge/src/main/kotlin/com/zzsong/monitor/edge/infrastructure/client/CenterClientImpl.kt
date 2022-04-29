package com.zzsong.monitor.edge.infrastructure.client

import cn.idealframework.extensions.reactor.ReactorResults
import cn.idealframework.extensions.reactor.Reactors
import cn.idealframework.json.TypeReference
import cn.idealframework.transmission.Result
import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.pojo.CollectPlan
import com.zzsong.monitor.common.req.ResourceDiscoveredArgs
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration

/**
 * 中心节点客户端实现
 *
 * @author 宋志宗 on 2022/3/26
 */
class CenterClientImpl(private val centerBaseUrl: String) : CenterClient {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(CenterClientImpl::class.java)
    private val COLLECT_PLAN_LIST_RESULT_REFERENCE =
      object : TypeReference<Result<List<CollectPlan>>>() {}
    private val webClient = Reactors.webClient {
      @Suppress("UsePropertyAccessSyntax")
      it.setKeepAlive(true)
        .setResponseTimeout(Duration.ofSeconds(5))
    }
  }

  override suspend fun resourceDiscovered(cluster: String, idents: Set<String>) {
    val args = ResourceDiscoveredArgs()
    args.cluster = cluster
    args.idents = idents
    val url = "$centerBaseUrl/monitor/resource/discovered"
    webClient.post().uri(url)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(args))
      .exchangeToMono { response ->
        val statusCode = response.statusCode()
        response.bodyToMono(String::class.java)
          .switchIfEmpty { Mono.just("") }
          .doOnNext { res ->
            if (!statusCode.is2xxSuccessful) {
              log.info("通知中心节点发现新资源执行失败: {}", res)
            }
          }
      }
      .onErrorResume {
        log.info("通知中心节点可能发现新资源执行异常: {} {}", it.javaClass.name, it.message)
        Mono.just("")
      }.subscribe()
  }

  override suspend fun findCollectPlan(cluster: String): List<CollectPlan> {
    val url = "$centerBaseUrl/monitor/collect_plan/find/cluster?cluster=$cluster"
    val result = webClient.get().uri(url)
      .exchangeToMono(ReactorResults.result(COLLECT_PLAN_LIST_RESULT_REFERENCE))
      .awaitSingle()
    return result.orThrow
  }

  override suspend fun findCollectPlan(cluster: String, type: CollectType): List<CollectPlan> {
    val url =
      "$centerBaseUrl/monitor/collect_plan/find/cluster_and_type?cluster=$cluster&type=$type"
    val result = webClient.get().uri(url)
      .exchangeToMono(ReactorResults.result(COLLECT_PLAN_LIST_RESULT_REFERENCE))
      .awaitSingle()
    return result.orThrow
  }

  override suspend fun findRecentlyModifiedCollectPlan(
    cluster: String,
    updatedTimeGte: Long
  ): List<CollectPlan> {
    val url =
      "$centerBaseUrl/monitor/collect_plan/find/recently_modified?cluster=$cluster&updatedTimeGte=$updatedTimeGte"
    val result = webClient.get().uri(url)
      .exchangeToMono(ReactorResults.result(COLLECT_PLAN_LIST_RESULT_REFERENCE))
      .awaitSingle()
    return result.orThrow
  }
}
