package br.com.jpbassinello.sbcgg.ratelimit.config;

import br.com.jpbassinello.sbcgg.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Method;

@Aspect
@Slf4j
class RateLimiterAspect {

  private static final String KEY_PREFIX = "rateLimiter";

  private final RateLimitClient remoteRateLimitClient;

  public RateLimiterAspect(RedisTemplate<String, String> redisTemplate) {
    remoteRateLimitClient = new RateLimitClient.RemoteRateLimitClient(redisTemplate);
    log.info("RateLimiterAspect initialized");
  }

  private void attemptRateLimit(Method method, String key, int limitPerPeriod, int periodInSeconds) {
    var rateLimitKey = String.format("%s::%s#%s::%s", KEY_PREFIX, method.getDeclaringClass().getName(), method.getName(), key);

    var rateLimited = remoteRateLimitClient.isRateLimited(rateLimitKey, limitPerPeriod, periodInSeconds);

    if (rateLimited) {
      log.warn("Rate Limit reached for key={}", rateLimitKey);
      throw new RateLimitException("Too many requests");
    }
  }

  @Before("@annotation(RateLimiter)")
  public void interceptor(JoinPoint joinPoint) {
    var method = ReflectionUtils.getMethod(joinPoint);
    var args = joinPoint.getArgs();
    var rateLimiter = method.getAnnotationsByType(RateLimiter.class)[0];

    String parsedSpelKey;
    try {
      parsedSpelKey = SpringUtils.parseSpel(method, args, rateLimiter.key());
    } catch (Exception e) {
      log.error("Failure parsing RateLimiter SPEL. Request allowed.", e);
      return;
    }

    attemptRateLimit(method, parsedSpelKey, rateLimiter.limitForPeriod(), rateLimiter.periodInSeconds());
  }
}