package com.zzsong.monitor.common.dto.req;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author 宋志宗 on 2022/3/19
 */
@Getter
@Setter
public class DeleteResourceArgs {
  /**
   * 集群编码
   *
   * @required
   */
  @Nullable
  private String cluster;

  /**
   * 资源标识列表
   *
   * @required
   */
  @Nullable
  private Set<String> idents;
}
