package br.com.jpbassinello.sbcgg.ai_client.application.port.out;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Caps the number of elements in a {@code List} record component when generating the
 * OpenAI Structured Outputs schema. Emitted as {@code maxItems} on the array schema and
 * hard-enforced by strict mode, this bounds the model's output so verbose arrays cannot
 * exhaust the token budget and truncate the response.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface MaxItems {
  int value();
}
