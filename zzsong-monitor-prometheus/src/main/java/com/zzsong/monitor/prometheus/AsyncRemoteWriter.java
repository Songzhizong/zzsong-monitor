package com.zzsong.monitor.prometheus;

import com.zzsong.monitor.common.pojo.TimeSeries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步远程写入客户端
 *
 * @author 宋志宗 on 2022/4/22
 */
public interface AsyncRemoteWriter {

  @Nonnull
  CompletableFuture<Boolean> remoteWrite(@Nullable List<TimeSeries> timeSeriesList);
}
