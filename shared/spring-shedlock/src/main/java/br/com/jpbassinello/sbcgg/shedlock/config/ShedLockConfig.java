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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@Import(RedisConfig.class)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT2S")
public class ShedLockConfig {

  @Bean
  public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockProvider(redisConnectionFactory);
  }

  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    var scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(10);
    scheduler.setThreadNamePrefix("scheduler-");
    return scheduler;
  }
}