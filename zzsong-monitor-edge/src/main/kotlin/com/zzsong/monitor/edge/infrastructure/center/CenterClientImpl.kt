package com.zzsong.monitor.edge.infrastructure.center

import cn.idealframework.extensions.reactor.Reactors
import com.zzsong.monitor.common.dto.req.ResourceDiscoveredArgs
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
    private val webClient = Reactors.webClient {
      @Suppress("UsePropertyAccessSyntax")
      it.setKeepAlive(true)
        .setResponseTimeout(Duration.ofSeconds(5))
    }
  }

  override fun resourceDiscovered(cluster: String, idents: Set<String>) {
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
}
