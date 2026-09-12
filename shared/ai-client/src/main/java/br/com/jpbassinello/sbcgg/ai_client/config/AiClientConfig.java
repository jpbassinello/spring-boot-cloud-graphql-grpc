package br.com.jpbassinello.sbcgg.ai_client.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiClientConfigProperties.class)
@ComponentScan(basePackages = "br.com.jpbassinello.sbcgg.ai_client.adapter.out")
public class AiClientConfig {
}
