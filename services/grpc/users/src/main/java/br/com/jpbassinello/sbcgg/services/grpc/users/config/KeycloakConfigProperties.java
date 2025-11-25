package br.com.jpbassinello.sbcgg.services.grpc.users.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "keycloak")
@Validated
public class KeycloakConfigProperties {

  private boolean active;
  private boolean initializeOnStartup;
  @NotEmpty
  private String masterRealm;
  @NotEmpty
  private String masterClient;
  @NotEmpty
  private String applicationRealm;
  @NotEmpty
  private String applicationClient;
  @NotEmpty
  private String serverUrl;
  @NotEmpty
  private String username;
  @NotEmpty
  private String password;

}
