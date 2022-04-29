package com.zzsong.monitor.center.port.http

import cn.idealframework.transmission.Result
import cn.idealframework.util.Asserts
import com.zzsong.monitor.center.application.CollectPlanService
import com.zzsong.monitor.center.args.CreateCollectPlanArgs
import com.zzsong.monitor.center.args.UpdateCollectPlanArgs
import com.zzsong.monitor.common.constants.CollectType
import com.zzsong.monitor.common.pojo.CollectPlan
import org.springframework.web.bind.annotation.*

/**
 * 采集计划管理
 *
 * @author 宋志宗 on 2022/4/22
 */
@RestController
@RequestMapping("/monitor/collect_plan")
class CollectPlanController(private val collectPlanService: CollectPlanService) {

  /**
   * 新建采集计划
   *
   * @author 宋志宗 on 2022/4/22
   */
  @PostMapping("/create")
  suspend fun create(@RequestBody(required = false) args: CreateCollectPlanArgs?): Result<CollectPlan> {
    Asserts.nonnull(args, "请求body为空");args!!
    val collectPlanDo = collectPlanService.create(args)
    val collectPlan = collectPlanDo.toCollectPlan()
    return Result.success(collectPlan)
  }

  /**
   * 更新采集计划信息
   *
   * @author 宋志宗 on 2022/4/24
   */
  @PostMapping("/update")
  suspend fun update(
    id: Long?,
    @RequestBody(required = false)
    args: UpdateCollectPlanArgs?
  ): Result<CollectPlan> {
    Asserts.nonnull(id, "id为空");id!!
    Asserts.nonnull(args, "请求body为空");args!!
    val collectPlanDo = collectPlanService.update(id, args)
    val collectPlan = collectPlanDo.toCollectPlan()
    return Result.success(collectPlan)
  }

  /**
   * 删除采集计划
   *
   * @author 宋志宗 on 2022/4/22
   */
  @PostMapping("/delete")
  suspend fun delete(id: Long): Result<Void> {
    collectPlanService.delete(id)
    return Result.success()
  }

  /**
   * 获取指定集群下所有的采集计划
   *
   * @ignore
   * @param cluster 集群编码 必填
   * @author 宋志宗 on 2022/4/22
   */
  @GetMapping("/find/cluster")
  suspend fun findByCluster(cluster: String?): Result<List<CollectPlan>> {
    Asserts.notBlank(cluster, "cluster为空");cluster!!
    val planDoList = collectPlanService.findAllByCluster(cluster)
    val planList = planDoList.map { it.toCollectPlan() }
    return Result.success(planList)
  }

  /**
   * 获取指定集群下某个采集类型的所有采集计划
   *
   * @ignore
   * @param cluster 集群编码 必填
   * @author 宋志宗 on 2022/4/22
   */
  @GetMapping("/find/cluster_and_type")
  suspend fun findByClusterAndType(
    cluster: String?,
    type: CollectType?
  ): Result<List<CollectPlan>> {
    Asserts.notBlank(cluster, "cluster为空");cluster!!
    Asserts.nonnull(type, "type为空");type!!
    val planDoList = collectPlanService.findAllByClusterAndCollectType(cluster, type)
    val planList = planDoList.map { it.toCollectPlan() }
    return Result.success(planList)
  }

  /**
   * 获取最近变更的采集计划
   *
   * 包含被删除的采集计划
   *
   * @param cluster        集群编码 必填
   * @param updatedTimeGte 时间戳,此时间之后发生变更的数据 必填
   * @author 宋志宗 on 2022/4/22
   */
  @GetMapping("/find/recently_modified")
  suspend fun findRecentlyModified(
    cluster: String?,
    updatedTimeGte: Long?
  ): Result<List<CollectPlan>> {
    Asserts.notBlank(cluster, "cluster为空");cluster!!
    Asserts.nonnull(updatedTimeGte, "updatedTimeGte为空");updatedTimeGte!!
    val planDoList =
      collectPlanService.findAllByClusterAndUpdatedTimeGte(cluster, updatedTimeGte)
    val planList = planDoList.map { it.toCollectPlan() }
    return Result.success(planList)
  }
}
