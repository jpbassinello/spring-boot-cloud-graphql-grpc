package br.com.jpbassinello.sbcgg.ratelimit.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ReflectionUtils {

  static Method getMethod(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    if (method.getDeclaringClass().isInterface()) {
      try {
        method = joinPoint
            .getTarget()
            .getClass()
            .getDeclaredMethod(joinPoint.getSignature().getName(),
                method.getParameterTypes());
      } catch (SecurityException | NoSuchMethodException e) {
        throw new IllegalStateException("Unexpected reflection error while trying to intercept call for AOP method ", e);
      }
    }
    return method;
  }
}
