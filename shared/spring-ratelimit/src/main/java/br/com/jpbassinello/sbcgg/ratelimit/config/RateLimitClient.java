package br.com.jpbassinello.sbcgg.ratelimit.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

interface RateLimitClient {

  boolean isRateLimited(String key, int limitForPeriod, int periodInSeconds);

  @Slf4j
  @RequiredArgsConstructor
  class RemoteRateLimitClient implements RateLimitClient {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isRateLimited(String key, int limitForPeriod, int periodInSeconds) {
      var redisKey = "rate:limit:" + key;

      System.out.println(redisKey);

      // Check if key exists
      var keyExists = Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));

      if (!keyExists) {
        // First request, initialize counter
        redisTemplate.opsForValue().set(redisKey, "", Duration.ofSeconds(periodInSeconds));
        return false;
      }

      // Increment counter
      Long currentCount = redisTemplate.opsForValue().increment(redisKey);

      if (currentCount == null) {
        // Something went wrong with Redis, allow by default but log error
        log.error("Failed to increment rate limit counter for client: {}", key);
        return true;
      }

      return currentCount <= limitForPeriod;
    }
  }
}