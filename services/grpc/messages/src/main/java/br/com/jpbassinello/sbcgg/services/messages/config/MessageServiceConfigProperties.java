package br.com.jpbassinello.sbcgg.services.messages.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "service.messages")
public class MessageServiceConfigProperties {

  private int maxRetries;
  private Duration nextAttemptWaitDuration;
}