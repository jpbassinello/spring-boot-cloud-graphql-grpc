package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ContextConfiguration(
    classes = {
        ManageUsersUseCase.class
    }
)
class ManageUsersServiceIT extends BaseServiceIT {

  @MockitoBean
  private SyncIdentityPort syncIdentityPort;

  @MockitoBean
  private VerifyUserUseCase verifyUser;

  @Autowired
  private LoadUserPort loadUser;

  @Autowired
  private ManageUsersUseCase service;

  @Test
  void registerAdmin() {

    var registeredAdmin = service.register(
        ManageUsersUseCase.RegisterUserInput.builder()
            .email("admin@my.app")
            .mobilePhoneNumber("+5519991038011")
            .firstName("App")
            .lastName("Admin")
            .password("password")
            .timeZoneId("America/Los_Angeles")
            .roles(List.of(Role.USER, Role.ADMIN))
            .build()
    );

    assertThat(registeredAdmin.getRoles()).hasSameElementsAs(EnumSet.of(Role.USER, Role.ADMIN));

    assertThat(Optional.of(registeredAdmin)).usingRecursiveComparison()
        .isEqualTo(loadUser.loadUserById(registeredAdmin.getId()));

    verify(syncIdentityPort).create(registeredAdmin, "password");
    verify(verifyUser).sendEmailVerificationCode(registeredAdmin.getId());
  }

  @Test
  void registerUser() {

    var registeredUser = service.register(
        ManageUsersUseCase.RegisterUserInput.builder()
            .email("user@my.app")
            .firstName("App")
            .lastName("User")
            .password("password")
            .timeZoneId("America/Sao_Paulo")
            .mobilePhoneNumber("+5519987654321")
            .roles(List.of(Role.USER))
            .build()
    );

    assertThat(registeredUser.getRoles()).hasSameElementsAs(EnumSet.of(Role.USER));

    assertThat(Optional.of(registeredUser)).usingRecursiveComparison()
        .isEqualTo(loadUser.loadUserById(registeredUser.getId()));

    verify(syncIdentityPort).create(registeredUser, "password");
    verify(verifyUser).sendEmailVerificationCode(registeredUser.getId());
  }

  @Test
  void failingValidation() {
    var input = ManageUsersUseCase.RegisterUserInput.builder().build();
    assertThatThrownBy(() -> service.register(input))
        .isInstanceOf(ConstraintViolationException.class)
        .matches(exception -> ((ConstraintViolationException) exception).getConstraintViolations().size() == 7);
    verify(verifyUser, never()).sendEmailVerificationCode(any());
    verify(syncIdentityPort, never()).create(any(), any());
  }

}
