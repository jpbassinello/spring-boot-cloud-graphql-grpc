package br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record VerifyUserContactMethodInput(
    @NotNull
    UUID userId,
    @NotNull
    UserContactMethod method,
    @NotEmpty
    String code
) {}
