package br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface SendMessagePort {

  void sendVerificationCodeMessage(UUID userId, String code);

  void sendForgotPasswordMessage(UUID userId, String code);

  void sendPasswordChangedMessage(UUID userId);

  void sendWelcomeMessage(UUID userId);
}
