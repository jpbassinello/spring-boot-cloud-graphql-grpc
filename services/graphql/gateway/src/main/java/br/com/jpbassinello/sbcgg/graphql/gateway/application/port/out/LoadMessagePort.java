package br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserMessagePage;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface LoadMessagePort {

  UserMessagePage searchMessages(UUID userId, int page);

}
