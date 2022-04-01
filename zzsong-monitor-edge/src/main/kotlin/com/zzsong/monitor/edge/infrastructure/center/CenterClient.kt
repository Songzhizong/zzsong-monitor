package com.zzsong.monitor.edge.infrastructure.center

/**
 * 中心节点客户端
 *
 * @author 宋志宗 on 2022/3/19
 */
interface CenterClient {

  fun resourceDiscovered(cluster: String, idents: Set<String>)
}
