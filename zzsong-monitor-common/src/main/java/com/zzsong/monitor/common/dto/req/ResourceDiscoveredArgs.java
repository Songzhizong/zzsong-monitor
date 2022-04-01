package com.zzsong.monitor.common.dto.req;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.Set;

/**
 * @author 宋志宗 on 2022/3/26
 */
@Getter
@Setter
public class ResourceDiscoveredArgs {
  private String cluster = "";

  private Set<String> idents = Collections.emptySet();
}
