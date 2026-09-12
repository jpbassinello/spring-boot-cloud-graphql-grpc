package br.com.jpbassinello.sbcgg.ratelimit.config;

import br.com.jpbassinello.sbcgg.exception.RateLimitException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterAspectTest {

  private static final int LIMIT_FOR_PERIOD = 5;
  private static final int PERIOD_IN_SECONDS = 1;
  @Mock
  private RedisTemplate<String, String> redisTemplate;
  @Mock
  private ValueOperations<String, String> valueOps;
  @Mock
  private JoinPoint joinPoint;
  @Mock
  private MethodSignature methodSignature;
  private RateLimiterAspect rateLimiterAspect;

  @Test
  void testExpectRateLimitException() throws NoSuchMethodException {
    rateLimiterAspect = new RateLimiterAspect(redisTemplate);

    var userId = UUID.randomUUID();
    var key = "rate:limit:rateLimiter::br.com.jpbassinello.sbcgg.ratelimit.config.RateLimiterAspectTest$DummyClass#test::" + userId;
    System.out.println(key);

    when(redisTemplate.hasKey(key)).thenReturn(true);

    when(valueOps.increment(key)).thenReturn(2L);
    when(redisTemplate.opsForValue()).thenReturn(valueOps);

    var method = DummyClass.class.getDeclaredMethod("test", UUID.class);
    when(joinPoint.getArgs()).thenReturn(new Object[]{userId});
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);

    assertThatCode(() -> rateLimiterAspect.interceptor(joinPoint))
        .isInstanceOf(RateLimitException.class);
  }

  public static class DummyClass {
    @RateLimiter(key = "#userId", limitForPeriod = LIMIT_FOR_PERIOD, periodInSeconds = PERIOD_IN_SECONDS)
    public void test(UUID userId) {
      fail("I should never be called.");
    }
  }
}