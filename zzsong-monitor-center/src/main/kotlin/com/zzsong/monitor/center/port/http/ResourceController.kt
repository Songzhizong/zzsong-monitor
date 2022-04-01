package com.zzsong.monitor.center.port.http

import cn.idealframework.transmission.Result
import cn.idealframework.util.Asserts
import com.zzsong.monitor.center.application.ResourceService
import com.zzsong.monitor.common.dto.req.ChangeResourceNoteArgs
import com.zzsong.monitor.common.dto.req.DeleteResourceArgs
import com.zzsong.monitor.common.dto.req.ResourceDiscoveredArgs
import com.zzsong.monitor.common.dto.req.ResourceTagArgs
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 监控目标管理
 *
 * @author 宋志宗 on 2022/3/19
 */
@Suppress("DuplicatedCode")
@RestController
@RequestMapping("/monitor/resource")
class ResourceController(private val resourceService: ResourceService) {

  /**
   * 发现资源
   *
   * 每当边缘节点新接收到指标推送时都有可能调用此接口将资源的ident发送给中心节点,
   * 但这并不意味着该资源一定是新增的, 也有可能是之前已经存在的
   *
   * @ignore
   * @author 宋志宗 on 2022/3/26
   */
  @PostMapping("/discovered")
  suspend fun discovered(@RequestBody args: ResourceDiscoveredArgs): Result<Unit> {
    resourceService.createIfAbsent(args.cluster, args.idents)
    return Result.success()
  }

  /**
   * 批量添加标签
   *
   * @author 宋志宗 on 2022/3/19
   */
  @PostMapping("/tag/add")
  suspend fun addTag(@RequestBody(required = false) args: ResourceTagArgs?): Result<Unit> {
    Asserts.nonnull(args, "请求body为空");args!!
    val cluster = args.cluster
    val idents = args.idents
    val tags = args.tags
    Asserts.notBlank(cluster, "集群编码不能为空");cluster!!
    Asserts.notEmpty(idents, "资源标识不能为空");idents!!
    resourceService.addTags(cluster, idents, tags)
    return Result.success()
  }

  /**
   * 批量移除标签
   *
   * @author 宋志宗 on 2022/3/19
   */
  @PostMapping("/tag/remove")
  suspend fun removeTag(@RequestBody(required = false) args: ResourceTagArgs?): Result<Unit> {
    Asserts.nonnull(args, "请求body为空");args!!
    val cluster = args.cluster
    val idents = args.idents
    val tags = args.tags
    Asserts.notBlank(cluster, "集群编码不能为空");cluster!!
    Asserts.notEmpty(idents, "资源标识不能为空");idents!!
    resourceService.removeTags(cluster, idents, tags)
    return Result.success()
  }

  /**
   * 批量修改备注
   *
   * @author 宋志宗 on 2022/3/19
   */
  @PostMapping("/change_note")
  suspend fun changeNote(@RequestBody(required = false) args: ChangeResourceNoteArgs?): Result<Unit> {
    Asserts.nonnull(args, "请求body为空");args!!
    val cluster = args.cluster
    val idents = args.idents
    val note = args.note
    Asserts.notBlank(cluster, "集群编码不能为空");cluster!!
    Asserts.notEmpty(idents, "资源标识不能为空");idents!!
    resourceService.changeNote(cluster, idents, note)
    return Result.success()
  }

  /**
   * 批量删除
   *
   * @author 宋志宗 on 2022/3/19
   */
  @PostMapping("/delete")
  suspend fun delete(@RequestBody(required = false) args: DeleteResourceArgs?): Result<Unit> {
    Asserts.nonnull(args, "请求body为空");args!!
    val cluster = args.cluster
    val idents = args.idents
    Asserts.notBlank(cluster, "集群编码不能为空");cluster!!
    Asserts.notEmpty(idents, "资源标识不能为空");idents!!
    resourceService.delete(cluster, idents)
    return Result.success()
  }
}
