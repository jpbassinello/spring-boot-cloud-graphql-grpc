package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.VerifyUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.UserVerificationCode;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
class UserVerificationCodePersistenceAdapter implements VerifyUserPort {

  private final UserVerificationCodeRepository repository;

  @Override
  public Optional<UserVerificationCode> isValid(UUID userId, String code, UserVerificationCodeType type, ZonedDateTime now) {
    return repository.findByUserIdAndCodeAndTypeAndValidUntilGreaterThanAndConfirmedAtIsNull(userId, code, type, now);
  }

  @Override
  public void save(UserVerificationCode verificationCode) {
    repository.save(verificationCode);
  }
}
