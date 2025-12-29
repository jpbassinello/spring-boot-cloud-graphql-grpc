package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.in;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs.SearchUserInput;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserPage;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@Controller
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
class UserQueryController {

  private final LoadUserPort loadUser;
  private final LoggedUserLoader loggedUser;

  @QueryMapping
  @PreAuthorize("hasRole('ADMIN')")
  public UserPage users(@Argument SearchUserInput input) {
    return loadUser.search(
        input.terms(),
        input.page(),
        input.pageSize()
    );
  }

  @QueryMapping
  @Nullable
  @PreAuthorize("hasRole('ADMIN')")
  public User user(@Argument UUID id) {
    return loadUser.loadUserById(id)
        .orElse(null);
  }

  @QueryMapping
  @Nullable
  public User logged() {
    return loggedUser.load();
  }

}
