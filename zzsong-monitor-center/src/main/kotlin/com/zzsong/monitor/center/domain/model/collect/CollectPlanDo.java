package com.zzsong.monitor.center.domain.model.collect;

import cn.idealframework.util.Asserts;
import com.zzsong.monitor.center.domain.model.cluster.ClusterDo;
import com.zzsong.monitor.center.domain.model.staff.BizGroupDo;
import com.zzsong.monitor.common.constants.CollectType;
import com.zzsong.monitor.common.pojo.CollectPlan;
import com.zzsong.monitor.common.pojo.PromCollectCfg;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 采集计划
 *
 * @author 宋志宗 on 2022/4/22
 */
@Getter
@Setter
@Document(CollectPlanDo.DOCUMENT_NAME)
@CompoundIndexes({
  @CompoundIndex(name = "bizGroupId", def = "{bizGroupId:1}", sparse = true),
  @CompoundIndex(name = "ident", def = "{ident:1}", sparse = true),
  @CompoundIndex(name = "cluster", def = "{cluster:1}"),
  @CompoundIndex(name = "updatedTime", def = "{updatedTime:-1}"),
})
public class CollectPlanDo {
  public static final String DOCUMENT_NAME = "monitor_collect";

  /** 主键 */
  @Id
  private long id = -1;

  /** 业务组id */
  private long bizGroupId = -1;

  /** 集群编码 */
  @Nonnull
  private String cluster = "";

  /** 资源标识 */
  @Nullable
  private String ident = null;

  /** 名称 */
  @Nonnull
  private String name = "";

  /** 采集类型 */
  @Nonnull
  private CollectType type = CollectType.PROMETHEUS;

  /** prometheus采集配置, 采集类型为PROMETHEUS必填 */
  @Nullable
  private PromCollectCfg prometheus;

  /** 备注 */
  @Nullable
  private String note;

  /** 是否为启用状态 */
  private boolean enabled = true;

  /** 是否已被删除 */
  private boolean deleted = false;

  /** 版本号 */
  @Version
  private long version = 0;

  /** 创建时间 */
  @CreatedDate
  private long createdTime;

  /** 更新时间 */
  @LastModifiedDate
  private long updatedTime;

  @Nonnull
  public static CollectPlanDo create(@Nullable BizGroupDo bizGroup,
                                     @Nonnull ClusterDo cluster,
                                     @Nullable String ident,
                                     @Nonnull String name,
                                     @Nonnull CollectType type,
                                     @Nullable PromCollectCfg prometheus,
                                     @Nullable String note) {
    CollectPlanDo collectPlanDo = new CollectPlanDo();
    if (bizGroup != null) {
      collectPlanDo.setBizGroupId(bizGroup.getId());
    }
    collectPlanDo.setCluster(cluster.getCode());
    collectPlanDo.setIdent(ident);
    collectPlanDo.setName(name);
    collectPlanDo.setType(type);
    if (type == CollectType.PROMETHEUS) {
      Asserts.nonnull(prometheus, "prometheus采集配置为空");
      prometheus.check();
      collectPlanDo.setPrometheus(prometheus);
    }
    collectPlanDo.setNote(note);
    return collectPlanDo;
  }

  public void update(@Nonnull String name,
                     @Nullable String note,
                     @Nullable PromCollectCfg prometheus) {
    CollectType type = getType();
    if (type == CollectType.PROMETHEUS) {
      Asserts.nonnull(prometheus, "prometheus采集配置为空");
      prometheus.check();
      setPrometheus(prometheus);
    }
    setName(name);
    setNote(note);
  }

  public void enable() {
    setEnabled(true);
  }

  public void disable() {
    setEnabled(false);
  }

  public void delete() {
    this.setDeleted(true);
  }

  @Nonnull
  public CollectPlan toCollectPlan() {
    CollectPlan collectPlan = new CollectPlan();
    collectPlan.setId(this.getId());
    collectPlan.setBizGroupId(this.getBizGroupId());
    collectPlan.setCluster(this.getCluster());
    collectPlan.setIdent(this.getIdent());
    collectPlan.setName(this.getName());
    collectPlan.setType(this.getType());
    collectPlan.setPrometheus(this.getPrometheus());
    collectPlan.setNote(this.getNote());
    collectPlan.setEnabled(this.getEnabled());
    collectPlan.setDeleted(this.getDeleted());
    collectPlan.setCreatedTime(this.getCreatedTime());
    collectPlan.setUpdatedTime(this.getUpdatedTime());
    return collectPlan;
  }
}
