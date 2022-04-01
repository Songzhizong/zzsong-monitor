package com.zzsong.monitor.center.domain.model.cluster;

import cn.idealframework.lang.StringUtils;
import cn.idealframework.util.Asserts;
import com.zzsong.monitor.common.constants.ConnectType;
import com.zzsong.monitor.common.pojo.Cluster;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 集群实体
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
@Document("monitor_cluster")
public class ClusterDo {

  /** 主键 */
  @Id
  private Long id;

  /** 集群编码 */
  @Nonnull
  @Indexed(name = "monitor_cluster_code", unique = true)
  private String code = "";

  /** 集群备注 */
  @Nonnull
  private String note = "";


  /** 访问地址 */
  @Nonnull
  private String address = "";

  /** 连接方式 */
  @Nonnull
  private ConnectType connectType = ConnectType.DIRECT;

  /** 版本号 */
  @Version
  private long version = 0;

  /** 创建时间 */
  @CreatedDate
  private long createdTime;

  /** 更新时间 */
  @LastModifiedDate
  private long updatedTime;

  public void update(@Nullable String note,
                     @Nullable String address,
                     @Nonnull ConnectType connectType) {
    if (connectType == ConnectType.DIRECT) {
      Asserts.notBlank(address, "直连模式下集群的连接地址不能为空");
    }
    this.setNote(note);
    this.setAddress(address);
    this.setConnectType(connectType);
  }

  @Nonnull
  public static ClusterDo create(@Nonnull String code,
                                 @Nullable String note,
                                 @Nullable String address,
                                 @Nonnull ConnectType connectType) {
    if (connectType == ConnectType.DIRECT) {
      Asserts.notBlank(address, "直连模式下集群的连接地址不能为空");
    }
    ClusterDo clusterDo = new ClusterDo();
    clusterDo.setCode(code);
    clusterDo.setNote(note);
    clusterDo.setAddress(address);
    clusterDo.setConnectType(connectType);
    return clusterDo;
  }

  @Nonnull
  public Cluster toCluster() {
    Cluster cluster = new Cluster();
    cluster.setId(this.getId());
    cluster.setCode(this.getCode());
    cluster.setNote(this.getNote());
    cluster.setAddress(this.getAddress());
    cluster.setConnectType(this.getConnectType());
    cluster.setCreatedTime(this.getCreatedTime());
    cluster.setUpdatedTime(this.getUpdatedTime());
    return cluster;
  }

  public void setCode(@Nonnull String code) {
    this.code = code;
  }

  public void setNote(@Nullable String note) {
    if (StringUtils.isBlank(note)) {
      note = "";
    }
    this.note = note;
  }

  public void setAddress(@Nullable String address) {
    if (StringUtils.isBlank(address)) {
      address = "";
    }
    this.address = address;
  }

  public void setConnectType(@Nonnull ConnectType connectType) {
    this.connectType = connectType;
  }
}
