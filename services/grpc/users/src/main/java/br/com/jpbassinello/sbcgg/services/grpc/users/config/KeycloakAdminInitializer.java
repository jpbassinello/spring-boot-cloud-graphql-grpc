package br.com.jpbassinello.sbcgg.services.grpc.users.config;

import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The intention of this class is to use `users` service as an initializer of the Keycloak server
 * It should initialize the realm and the client and can potentially initialize default users as well
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
class KeycloakAdminInitializer {

  private final SyncIdentityPort syncIdentityPort;

  @PostConstruct
  void initialize() {

    log.info("Syncing admin account with identity provider");
    syncIdentityPort.create(
        User.builder()
            .email("admin@sbcgg.com")
            .firstName("Admin")
            .lastName("MyApp")
            .active(true)
            .emailVerified(true)
            .mobilePhoneNumber("+5519999998888")
            .mobilePhoneNumberVerified(true)
            .roles(List.of(Role.ADMIN, Role.USER))
            .build(), "admin");
  }
}
