package com.zzsong.monitor.prometheus;

import com.zzsong.monitor.common.pojo.TimeSeries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 远程写入客户端
 *
 * @author 宋志宗 on 2022/4/22
 */
public interface RemoteWriter {

  void remoteWrite(@Nullable List<TimeSeries> timeSeriesList);
}
