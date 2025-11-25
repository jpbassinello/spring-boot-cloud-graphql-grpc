package br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterUserInput(
    @NotEmpty(message = "user.email.NotEmpty")
    @Size(max = 150, message = "user.email.Size")
    @Email(message = "user.email.Invalid")
    String email,
    @NotEmpty(message = "user.mobilePhoneNumber.NotEmpty")
    @Size(max = 150, message = "user.mobilePhoneNumber.Size")
    String mobilePhoneNumber,
    @NotEmpty(message = "user.firstName.NotEmpty")
    @Size(max = 255, message = "user.firstName.Size")
    String firstName,
    @NotEmpty(message = "user.lastName.NotEmpty")
    @Size(max = 255, message = "user.lastName.Size")
    String lastName,
    @NotEmpty(message = "user.password.NotEmpty")
    @Size(max = 150, message = "user.password.Size")
    String password,
    @NotEmpty(message = "user.timeZoneId.NotEmpty")
    String timeZoneId
) {}
