package com.zzsong.monitor.common.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 监控目标信息
 *
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class Resource {

  /** 主键 */
  private long id;

  /** 业务组id */
  private long bizGroupId;

  /** 集群id */
  private String cluster;

  /** 资源标识 */
  private String ident;

  /** 标签列表 */
  private Set<String> tags = new LinkedHashSet<>();

  /** 资源备注 */
  private String note;

  /** 创建时间 */
  private long createdTime;

  /** 更新时间 */
  private long updatedTime;
}
