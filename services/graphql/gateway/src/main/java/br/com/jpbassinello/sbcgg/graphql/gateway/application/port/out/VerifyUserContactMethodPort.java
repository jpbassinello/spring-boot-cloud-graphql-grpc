package br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.RegisterUserInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface VerifyUserContactMethodPort {

  void verifyUserContactMethod(UUID userId, String code, UserContactMethod userContactMethod);

}
