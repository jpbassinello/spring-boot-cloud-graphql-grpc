package br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.Role;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.RegisterUserInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public interface RegisterUserPort {

  User register(RegisterUserInput input, List<Role> roles);

}
