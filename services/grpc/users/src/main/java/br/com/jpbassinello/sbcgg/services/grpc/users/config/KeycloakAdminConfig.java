package br.com.jpbassinello.sbcgg.services.grpc.users.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakConfigProperties.class)
class KeycloakAdminConfig {

  @Bean
  Keycloak keycloak(KeycloakConfigProperties properties) {
    return KeycloakBuilder.builder()
        .grantType(OAuth2Constants.PASSWORD)
        .realm(properties.getMasterRealm())
        .clientId(properties.getMasterClient())
        .username(properties.getUsername())
        .password(properties.getPassword())
        .serverUrl(properties.getServerUrl())
        .build();
  }
}
