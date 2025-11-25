package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.PersistUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SendMessagePort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.VerifyUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.UserVerificationCode;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyUserUseCaseTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final User USER =  User.builder()
      .id(USER_ID)
      .firstName("João")
      .lastName("Bassinello")
      .email("joao@sbcgg.com")
      .build();

  @Mock
  private VerifyUserPort verifyUser;
  @Mock
  private LoadUserPort loadUser;
  @Mock
  private PersistUserPort persistUser;
  @Mock
  private SyncIdentityPort syncIdentity;
  @Mock
  private SendMessagePort sendMessage;
  @Mock
  private TimeNow timeNow;

  @InjectMocks
  private VerifyUserUseCase service;

  @BeforeEach
  void init() {
    when(loadUser.loadUserById(USER_ID))
        .thenReturn(
            Optional.of(
                User.builder()
                    .id(USER_ID)
                    .firstName("João")
                    .lastName("Bassinello")
                    .email("joao@sbcgg.com")
                    .build()
            )
        );
  }

  @Test
  void sendVerificationCode() {
    var now = ZonedDateTime.now().plusMinutes(4);
    when(timeNow.get()).thenReturn(now);

    service.sendEmailVerificationCode(USER_ID);

    var captor = ArgumentCaptor.forClass(UserVerificationCode.class);
    verify(verifyUser).save(captor.capture());
    assertThat(Integer.parseInt(captor.getValue().getCode())).isBetween(0, 10_000);
    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .ignoringFields("code")
        .isEqualTo(
            UserVerificationCode.builder()
                .userId(USER_ID)
                .source("joao@sbcgg.com")
                .type(UserVerificationCodeType.EMAIL)
                .validUntil(now.plusDays(1))
                .build()
        );

    verify(sendMessage).sendVerificationCodeMessage(USER_ID, captor.getValue().getCode());
  }

  @Test
  void verifyUser() {

    var now =ZonedDateTime.now();
    when(timeNow.get()).thenReturn(now);

    when(verifyUser.isValid(USER_ID, "1234", UserVerificationCodeType.EMAIL, now))
        .thenReturn(
            Optional.of(
                UserVerificationCode.builder().build()
            )
        );

    when(persistUser.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

    service.verify(
        VerifyUserUseCase.VerifyRequest.builder()
            .userId(USER_ID)
            .type(UserVerificationCodeType.EMAIL)
            .code("1234")
            .build()
    );

    verify(verifyUser).save(
        UserVerificationCode.builder()
            .confirmedAt(now)
            .build()
    );

    verify(persistUser).save(
        User.builder()
            .id(USER_ID)
            .firstName("João")
            .lastName("Bassinello")
            .email("joao@sbcgg.com")
            .emailVerified(true)
            .active(true)
            .build()
    );

    verify(syncIdentity).setUserEmailVerifiedFlag("joao@sbcgg.com", true);
  }
}
