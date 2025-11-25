package br.com.jpbassinello.sbcgg.graphql.gateway.domain.types;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record User(
    UUID id,
    String email,
    boolean emailVerified,
    String mobilePhoneNumber,
    boolean mobilePhoneNumberVerified,
    String firstName,
    String lastName,
    boolean active,
    String timeZoneId,
    List<Role> roles,
    UserMessagePage messages
) {}
