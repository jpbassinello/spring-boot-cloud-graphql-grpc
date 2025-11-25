package br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out;

import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SyncIdentityPort {

  void create(User user, String password);

  void setUserEmailVerifiedFlag(String email, boolean verified);
}
