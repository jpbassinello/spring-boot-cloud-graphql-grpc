package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.identity;

import br.com.jpbassinello.sbcgg.exception.InternalServerErrorException;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.config.KeycloakConfigProperties;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "keycloak.active", havingValue = "true")
class KeycloakAdapter implements SyncIdentityPort {

  private static final KeycloakUserMapper MAPPER = KeycloakUserMapper.INSTANCE;
  private final ConcurrentMap<Role, RoleRepresentation> keycloakRoleIdByEnum = new ConcurrentHashMap<>();
  private final Keycloak keycloak;
  private final KeycloakConfigProperties properties;

  private Optional<UserRepresentation> getUserByEmail(String email) {
    return Optional.of(
            keycloak
                .realm(properties.getApplicationRealm())
                .users()
                .search(email)
        ).filter(list -> !list.isEmpty())
        .map(List::getFirst);
  }

  @Override
  public void create(User user, String password) {
    var userRepresentation = MAPPER.mapToKeycloakRepresentation(user, password);

    try (
        var response = keycloak
            .realm(properties.getApplicationRealm())
            .users()
            .create(userRepresentation)
    ) {

      var createdUserId = getUserByEmail(user.getEmail())
          .orElseThrow()
          .getId();

      keycloak
          .realm(properties.getApplicationRealm())
          .users()
          .get(createdUserId)
          .roles()
          .realmLevel()
          .add(user.getRoles().stream().map(this::getRoleRepresentation).toList());

      log.info("Created user id={} in Keycloak response={}", user.getId(), response.getStatus());
    } catch (Exception e) {
      throw new InternalServerErrorException("Error creating user in Keycloak", e);
    }
  }

  private RoleRepresentation getRoleRepresentation(Role role) {
    var representation = keycloakRoleIdByEnum.get(role);
    if (representation == null) {
      representation = keycloak
          .realm(properties.getApplicationRealm())
          .roles()
          .get(role.name())
          .toRepresentation();
      var existingRepresentation = keycloakRoleIdByEnum.putIfAbsent(role, representation);
      if (existingRepresentation != null) {
        return existingRepresentation;
      }
    }
    return representation;
  }

  @Override
  public void setUserEmailVerifiedFlag(String email, boolean verified) {
    var keycloakUser = keycloak
        .realm(properties.getApplicationRealm())
        .users()
        .search(email)
        .getFirst();

    keycloakUser.setEmailVerified(verified);

    keycloak
        .realm(properties.getApplicationRealm())
        .users()
        .get(keycloakUser.getId())
        .update(keycloakUser);
  }
}
