package br.com.jpbassinello.sbcgg.shedlock.config;

import br.com.jpbassinello.sbcgg.cache.config.RedisConfig;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Import(RedisConfig.class)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT2S")
public class ShedLockConfig {

  @Bean
  public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockProvider(redisConnectionFactory);
  }
}