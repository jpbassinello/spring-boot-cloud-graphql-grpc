package br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface VerifyUserContactMethodPort {

  void verifyUserContactMethod(UUID userId, String code, UserContactMethod userContactMethod);

}
