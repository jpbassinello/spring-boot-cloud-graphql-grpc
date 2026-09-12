package br.com.jpbassinello.sbcgg.ratelimit.config;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
@Documented
public @interface RateLimiter {

  /**
   * Key of the rate limiter. It can be SpEL expression
   * It will be concatenated to class name + method name
   * Default is empty key, so just method name will be used as key
   *
   * @return the key of the limiter
   */
  String key() default "";

  /**
   * How many calls same key is allowed do to in the period
   *
   * @return number of calls allowed for key
   */
  int limitForPeriod();

  /**
   * Period to limit the calls in seconds
   *
   * @return seconds limit
   */
  int periodInSeconds();
}
