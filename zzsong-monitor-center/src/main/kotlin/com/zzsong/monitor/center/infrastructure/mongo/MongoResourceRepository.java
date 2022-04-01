package com.zzsong.monitor.center.infrastructure.mongo;

import com.zzsong.monitor.center.domain.model.resource.ResourceDo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author 宋志宗 on 2022/3/19
 */
public interface MongoResourceRepository extends ReactiveMongoRepository<ResourceDo, Long> {

  @Nonnull
  Mono<ResourceDo> findByClusterAndIdent(@Nonnull String cluster, @Nonnull String ident);

  @Nonnull
  Flux<ResourceDo> findAllByClusterAndIdentIn(@Nonnull String cluster, @Nonnull Collection<String> idents);
}
