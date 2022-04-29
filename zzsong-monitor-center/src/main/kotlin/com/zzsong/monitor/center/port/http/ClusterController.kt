package com.zzsong.monitor.center.port.http

import cn.idealframework.transmission.Result
import cn.idealframework.util.Asserts
import com.zzsong.monitor.center.application.ClusterService
import com.zzsong.monitor.center.args.CreateClusterArgs
import com.zzsong.monitor.center.args.UpdateClusterArgs
import com.zzsong.monitor.common.pojo.Cluster
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 集群管理
 *
 * @author 宋志宗 on 2022/3/19
 */
@RestController
@RequestMapping("/monitor/cluster")
class ClusterController(private val clusterService: ClusterService) {

  /**
   * 创建集群
   *
   * @author 宋志宗 on 2022/3/19
   */
  @PostMapping("/create")
  suspend fun create(@RequestBody(required = false) args: CreateClusterArgs?): Result<Cluster> {
    Asserts.nonnull(args, "请求body为空");args!!
    val code = args.code
    val note = args.note
    val address = args.address
    val connectType = args.connectType
    Asserts.notBlank(code, "集群编码不能为空");code!!
    Asserts.nonnull(connectType, "连接类型不能为空");connectType!!
    val clusterDo = clusterService.create(code, note, address, connectType)
    val cluster = clusterDo.toCluster()
    return Result.success(cluster)
  }

  /**
   * 更新集群信息
   *
   * @author 宋志宗 on 2022/3/19
   */
  @PostMapping("/update")
  suspend fun update(
    id: Long?,
    @RequestBody(required = false) args: UpdateClusterArgs
  ): Result<Cluster> {
    val note = args.note
    val address = args.address
    val connectType = args.connectType
    Asserts.nonnull(id, "id不能为空");id!!
    Asserts.nonnull(connectType, "连接类型不能为空");connectType!!
    val clusterDo = clusterService.update(id, note, address, connectType)
    val cluster = clusterDo.toCluster()
    return Result.data(cluster)
  }

  @PostMapping("/delete")
  suspend fun delete(id: Long?): Result<Unit> {
    Asserts.nonnull(id, "id不能为空");id!!
    clusterService.delete(id)
    return Result.success()
  }
}
