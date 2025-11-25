package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.in;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggedUserLoader {

  private final LoadUserPort loadUserPort;

  public User load() {
    var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    var email = (String) jwt.getClaims().get("preferred_username");
    return loadUserPort.loadUserByEmail(email)
        .orElse(null);
  }

}
