package com.zzsong.monitor.center.application

import cn.idealframework.transmission.exception.BadRequestException
import com.zzsong.monitor.center.domain.model.cluster.ClusterDo
import com.zzsong.monitor.center.domain.model.cluster.ClusterRepository
import com.zzsong.monitor.common.constants.ConnectType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * @author 宋志宗 on 2022/3/19
 */
@Service
class ClusterService(private val clusterRepository: ClusterRepository) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(ClusterService::class.java)
  }

  /**
   * 创建集群
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun create(
    code: String,
    note: String?,
    address: String?,
    connectType: ConnectType
  ): ClusterDo {
    clusterRepository.findByCode(code)?.also {
      log.info("集群编码: {} 已被使用", code)
      throw BadRequestException("集群编码已被使用")
    }
    var clusterDo = ClusterDo.create(code, note, address, connectType)
    clusterDo = clusterRepository.save(clusterDo)
    log.info("新增集群: [{} {}]", clusterDo.code, clusterDo.note)
    return clusterDo
  }

  /**
   * 更新集群
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun update(
    id: Long,
    note: String?,
    address: String?,
    connectType: ConnectType
  ): ClusterDo {
    val clusterDo = clusterRepository.findRequiredById(id)
    clusterDo.update(note, address, connectType)
    clusterRepository.save(clusterDo)
    return clusterDo
  }

  /**
   * 删除集群
   *
   * @author 宋志宗 on 2022/3/19
   */
  suspend fun delete(id: Long) {
    val clusterDo = clusterRepository.findById(id)
    if (clusterDo == null) {
      log.info("需要删除的集群: {} 不存在", id)
      return
    }
    clusterRepository.delete(clusterDo)
  }
}
