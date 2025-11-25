package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.in;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.RegisterUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.VerifyUserContactMethodPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.RegisterUserInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.VerifyUserContactMethodInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Validated
class UserMutationController {

  private final RegisterUserPort registerUser;
  private final VerifyUserContactMethodPort verifyUserContactMethod;

  @MutationMapping
  @PreAuthorize("hasRole('ADMIN')")
  public User registerAdmin(@Argument @Valid RegisterUserInput input) {
    return registerUser.register(input, List.of(Role.ADMIN, Role.USER));
  }

  @MutationMapping
  public User registerUser(@Argument @Valid RegisterUserInput input) {
    return registerUser.register(input, List.of(Role.USER));
  }

  @MutationMapping
  public boolean verifyUserContactMethod(@Argument @Valid VerifyUserContactMethodInput input) {
    verifyUserContactMethod.verifyUserContactMethod(
        input.userId(),
        input.code(),
        input.method()
    );
    return true;
  }
}
