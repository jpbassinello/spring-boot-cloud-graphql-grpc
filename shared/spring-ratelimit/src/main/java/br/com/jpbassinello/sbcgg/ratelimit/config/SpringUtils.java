package br.com.jpbassinello.sbcgg.ratelimit.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;

import java.lang.reflect.Method;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SpringUtils {
  private static final ExpressionParser PARSER = new SpelExpressionParser();
  private static final StandardReflectionParameterNameDiscoverer DISCOVERER = new StandardReflectionParameterNameDiscoverer();

  public static String parseSpel(Method method, Object[] arguments, String spel) {

    if (spel == null || spel.isBlank()) {
      return "";
    }

    String[] params = Optional.ofNullable(DISCOVERER.getParameterNames(method)).orElse(new String[]{});
    var context = new StandardEvaluationContext();
    for (int len = 0; len < params.length; len++) {
      context.setVariable(params[len], arguments[len]);
    }

    var conversionService = new DefaultConversionService();
    var typeConverter = new StandardTypeConverter(conversionService);
    context.setTypeConverter(typeConverter);

    var expression = PARSER.parseExpression(spel);
    return expression.getValue(context, String.class);
  }
}
