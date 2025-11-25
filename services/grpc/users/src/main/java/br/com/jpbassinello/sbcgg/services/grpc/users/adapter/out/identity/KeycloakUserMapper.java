package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.identity;

import br.com.jpbassinello.sbcgg.mapstruct.DefaultMapstructConfig;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(config = DefaultMapstructConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface KeycloakUserMapper {

  KeycloakUserMapper INSTANCE = Mappers.getMapper(KeycloakUserMapper.class);

  @Mapping(target = "username", source = "user.email")
  @Mapping(target = "enabled", source = "user.active")
  @Mapping(target = "realmRoles", source = "user.roles")
  @Mapping(target = "credentials", expression = "java(mapCredentials(password))")
  UserRepresentation mapToKeycloakRepresentation(User user, String password);

  default List<CredentialRepresentation> mapCredentials(String password) {
    var userCredentialRepresentation = new CredentialRepresentation();
    userCredentialRepresentation.setType(CredentialRepresentation.PASSWORD);
    userCredentialRepresentation.setTemporary(false);
    userCredentialRepresentation.setValue(password);
    return List.of(userCredentialRepresentation);
  }

}
