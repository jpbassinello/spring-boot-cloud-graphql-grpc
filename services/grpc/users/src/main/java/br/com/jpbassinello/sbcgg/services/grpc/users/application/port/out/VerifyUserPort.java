package br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out;

import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.UserVerificationCode;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface VerifyUserPort {

  Optional<UserVerificationCode> isValid(
      UUID userId,
      String code,
      UserVerificationCodeType type,
      ZonedDateTime now
  );

  void save(UserVerificationCode verificationCode);
}
