package com.zzsong.monitor.center.domain.model.resource;

import cn.idealframework.lang.CollectionUtils;
import cn.idealframework.lang.StringUtils;
import com.zzsong.monitor.center.domain.model.staff.BizGroupDo;
import com.zzsong.monitor.common.pojo.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 监控目标实体
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
@Document("monitor_resource")
@CompoundIndexes({
  @CompoundIndex(name = "uk_monitor_resource_cluster_ident", def = "{cluster:1, ident:1}", unique = true)
})
public class ResourceDo {

  /** 主键 */
  @Id
  private long id = -1;

  /** 业务组id */
  @Indexed(name = "monitor_resource_group")
  private long bizGroupId = -1;

  /** 集群id */
  private String cluster = "";

  /** 资源标识 */
  private String ident = "";

  /** 标签列表 */
  @Nonnull
  private Set<String> tags = new LinkedHashSet<>();

  /** 资源备注 */
  @Nonnull
  private String note = "";

  /** 查询标识 */
  @Nonnull
  @TextIndexed
  private String keyword = "";

  /** 版本号 */
  @Version
  private long version = 0;

  /** 创建时间 */
  @CreatedDate
  private long createdTime;

  /** 更新时间 */
  @LastModifiedDate
  @Indexed(name = "monitor_resource_updated_time", direction = IndexDirection.DESCENDING)
  private long updatedTime;

  /**
   * 添加标签
   *
   * @param tags 新添加的标签
   * @author 宋志宗 on 2022/3/19
   */
  public void addTags(@Nullable Collection<String> tags) {
    if (CollectionUtils.isEmpty(tags)) {
      return;
    }
    LinkedHashSet<String> newTags = new LinkedHashSet<>(getTags());
    newTags.addAll(tags);
    setTags(newTags);
    resetKeyword();
  }

  /**
   * 移除标签
   *
   * @param tags 移除的标签列表
   * @author 宋志宗 on 2022/3/19
   */
  public void removeTags(@Nullable Collection<String> tags) {
    if (CollectionUtils.isEmpty(tags)) {
      return;
    }
    LinkedHashSet<String> newTags = new LinkedHashSet<>(getTags());
    newTags.removeAll(tags);
    setTags(newTags);
    resetKeyword();
  }

  /**
   * 修改备注
   *
   * @param note 备注信息
   * @author 宋志宗 on 2022/3/19
   */
  public void changeNote(@Nullable String note) {
    this.setNote(note);
  }

  /**
   * 修改业务组
   *
   * @param group 业务组对象, 为null代表移出业务组
   * @author 宋志宗 on 2022/3/19
   */
  public void changeGroup(@Nullable BizGroupDo group) {
    long bizGroupId = -1;
    if (group != null) {
      bizGroupId = group.getId();
    }
    this.setBizGroupId(bizGroupId);
  }

  @Nonnull
  public static ResourceDo create(@Nonnull String cluster,
                                  @Nonnull String ident) {
    ResourceDo resourceDo = new ResourceDo();
    resourceDo.setCluster(cluster);
    resourceDo.setIdent(ident);
    return resourceDo;
  }

  @Nonnull
  public Resource toResource() {
    Resource resource = new Resource();
    resource.setId(this.getId());
    resource.setBizGroupId(this.getBizGroupId());
    resource.setCluster(this.getCluster());
    resource.setIdent(this.getIdent());
    resource.setTags(this.getTags());
    resource.setNote(this.getNote());
    resource.setCreatedTime(this.getCreatedTime());
    resource.setUpdatedTime(this.getUpdatedTime());
    return resource;
  }

  private void resetKeyword() {
    String ident = getIdent();
    Set<String> tags = getTags();
    StringBuilder sb = new StringBuilder(ident);
    for (String tag : tags) {
      sb.append(" ").append(tag);
    }
    setKeyword(sb.toString());
  }

  public void setBizGroupId(long bizGroupId) {
    this.bizGroupId = bizGroupId;
  }

  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  public void setIdent(String ident) {
    this.ident = ident;
  }

  public void setTags(@Nonnull Set<String> tags) {
    this.tags = tags;
  }

  public void setNote(@Nullable String note) {
    if (StringUtils.isBlank(note)) {
      note = "";
    }
    this.note = note;
  }

  public void setKeyword(@Nonnull String keyword) {
    this.keyword = keyword;
  }
}
