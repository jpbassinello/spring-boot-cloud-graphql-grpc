package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.in;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserMessagePage;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.ParametersAreNonnullByDefault;

@Controller
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
class UserMessagesResolver {

  private final LoadMessagePort loadMessagePort;

  @SchemaMapping
  public UserMessagePage messages(User user, @Argument int page) {
    return loadMessagePort.searchMessages(user.id(), page);
  }

}
