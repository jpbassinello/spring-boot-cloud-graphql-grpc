package br.com.jpbassinello.sbcgg.ratelimit.config;

import br.com.jpbassinello.sbcgg.cache.config.RedisConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RedisConfig.class)
@ComponentScan
public class RateLimitConfig {
}