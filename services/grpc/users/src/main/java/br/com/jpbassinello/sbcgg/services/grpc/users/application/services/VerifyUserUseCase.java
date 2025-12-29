package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.exception.BadRequestException;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.PersistUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SendMessagePort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.VerifyUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.UserVerificationCode;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.ParametersAreNonnullByDefault;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
@ParametersAreNonnullByDefault
@Slf4j
public class VerifyUserUseCase {

  private static final Duration EMAIL_VERIFICATION_CODE_DURATION = Duration.ofDays(1);

  private final SecureRandom secureRandom = new SecureRandom();

  private final VerifyUserPort verifyUser;
  private final LoadUserPort loadUser;
  private final PersistUserPort persistUserPort;
  private final SyncIdentityPort syncIdentity;
  private final SendMessagePort sendMessage;
  private final TimeNow timeNow;

  public void sendEmailVerificationCode(UUID userId) {
    var user = getUser(userId);
    // 6 digit number
    var verificationCode = StringUtils.leftPad(String.valueOf(secureRandom.nextInt(10_000)), 4, '0');
    verifyUser.save(
        UserVerificationCode.builder()
            .userId(userId)
            .source(user.getEmail())
            .code(verificationCode)
            .type(UserVerificationCodeType.EMAIL)
            .validUntil(timeNow.get().plus(EMAIL_VERIFICATION_CODE_DURATION))
            .build()
    );
    sendMessage.sendVerificationCodeMessage(userId, verificationCode);
  }

  @Transactional
  public void verify(@Valid VerifyRequest request) {
    var validCode = verifyUser.isValid(request.userId(), request.code(), request.type(), timeNow.get());

    if (validCode.isEmpty()) {
      log.warn("Bad code verification request for user_id={} type={}", request.userId(), request.type());
      throw new BadRequestException("Bad code verification request")
          .withViolationCodes(List.of("user.verification.Invalid"));
    }

    var entity = validCode.get();
    entity.setConfirmedAt(timeNow.get());
    verifyUser.save(entity);

    var user = getUser(request.userId());
    if (request.type() == UserVerificationCodeType.EMAIL) {
      user.setEmailVerified(true);
      user.setActive(true);
    } else {
      user.setMobilePhoneNumberVerified(true);
    }
    user = persistUserPort.save(user);

    if (request.type() == UserVerificationCodeType.EMAIL) {
      var email = user.getEmail();
      syncIdentity.setUserEmailVerifiedFlag(email, true);
    }
  }

  private User getUser(UUID userId) {
    return loadUser.loadUserById(userId).orElseThrow(() -> new IllegalArgumentException("User not found for id=" + userId));
  }

  @Builder
  public record VerifyRequest(
      UUID userId,
      UserVerificationCodeType type,
      @NotEmpty
      String code
  ) {}

}
