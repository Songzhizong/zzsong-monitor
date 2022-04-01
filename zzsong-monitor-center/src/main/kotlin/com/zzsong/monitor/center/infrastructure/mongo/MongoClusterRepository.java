package com.zzsong.monitor.center.infrastructure.mongo;

import com.zzsong.monitor.center.domain.model.cluster.ClusterDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗 on 2022/3/19
 */
@Repository
public interface MongoClusterRepository extends ReactiveMongoRepository<ClusterDo, Long> {

  @Nonnull
  Mono<ClusterDo> findByCode(@Nonnull String code);
}
