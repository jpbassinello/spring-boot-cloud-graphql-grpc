package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.identity;

import br.com.jpbassinello.sbcgg.services.grpc.users.config.KeycloakConfigProperties;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakAdapterTest {

  @Mock
  private RealmResource realmResource;
  @Mock
  private UsersResource usersResource;
  @Mock
  private UserResource userResource;
  @Mock
  private RoleMappingResource roleMappingResource;
  @Mock
  private RoleScopeResource roleScopeResource;
  @Mock
  private RolesResource rolesResource;
  @Mock
  private RoleResource roleResource;
  @Mock
  private Response response;

  @Mock
  private Keycloak keycloak;
  @Mock
  private KeycloakConfigProperties properties;

  @InjectMocks
  private KeycloakAdapter keycloakAdapter;

  @Test
  void create() {

    var keycloakUserRepresentation = new UserRepresentation();
    var keycloakUserId = UUID.randomUUID().toString();
    keycloakUserRepresentation.setId(keycloakUserId);

    var keycloakRoleRepresentation = new RoleRepresentation();
    var keycloakRoleId = UUID.randomUUID().toString();
    keycloakRoleRepresentation.setId(keycloakRoleId);

    when(properties.getApplicationRealm()).thenReturn("sbcgg");
    when(usersResource.create(any())).thenReturn(response);
    when(usersResource.search("email@sbcgg.com")).thenReturn(List.of(keycloakUserRepresentation));
    when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(usersResource.get(keycloakUserId)).thenReturn(userResource);
    when(realmResource.users()).thenReturn(usersResource);
    when(roleResource.toRepresentation()).thenReturn(keycloakRoleRepresentation);
    when(rolesResource.get(anyString())).thenReturn(roleResource);
    when(realmResource.roles()).thenReturn(rolesResource);
    when(keycloak.realm("sbcgg")).thenReturn(realmResource);

    var userId = UUID.randomUUID();
    var user = User.builder()
        .id(userId)
        .email("email@sbcgg.com")
        .firstName("John")
        .lastName("Doe")
        .emailVerified(true)
        .roles(List.of(Role.USER))
        .build();

    keycloakAdapter.create(user, "password");

    var captor = ArgumentCaptor.forClass(UserRepresentation.class);

    verify(usersResource).create(captor.capture());

    var expected = new UserRepresentation();
    expected.setEmail(user.getEmail());
    expected.setUsername(user.getEmail());
    expected.setId(user.getId().toString());
    expected.setEnabled(user.isActive());
    expected.setEmailVerified(user.isEmailVerified());
    expected.setRealmRoles(user.getRoles().stream().map(Enum::name).toList());
    expected.setFirstName(user.getFirstName());
    expected.setLastName(user.getLastName());
    var credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setTemporary(false);
    credential.setValue("password");
    expected.setCredentials(List.of(credential));

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(expected);

    var captorForRole = ArgumentCaptor.forClass(List.class);

    verify(roleScopeResource).add(captorForRole.capture());
    assertThat(captorForRole.getValue().size()).isEqualTo(1);
    assertThat(captorForRole.getValue().getFirst()).isEqualTo(keycloakRoleRepresentation);
  }

}
