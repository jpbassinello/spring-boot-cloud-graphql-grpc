package br.com.jpbassinello.sbcgg.services.grpc.users.application.services;

import br.com.jpbassinello.sbcgg.exception.BadRequestException;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.PersistUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SyncIdentityPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import br.com.jpbassinello.sbcgg.validation.MobilePhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@ParametersAreNonnullByDefault
@Slf4j
public class ManageUsersUseCase {

  private final PersistUserPort persistUser;
  private final LoadUserPort loadUser;
  private final SyncIdentityPort syncIdentity;
  private final VerifyUserUseCase verifyUser;

  @Transactional
  public User register(@Valid RegisterUserInput input) {
    var isAdmin = input.roles().contains(Role.ADMIN);
    var email = input.email();
    var mobilePhoneNumber = input.mobilePhoneNumber();
    log.info("Registering user email={} mobile_phone_number={} is_admin={}",
        email, mobilePhoneNumber, isAdmin);

    var existsWitEmail = loadUser.loadUserByEmail(email);
    if (existsWitEmail.isPresent()) {
      throw new BadRequestException("User already exists with email " + email)
          .withViolationCodes(List.of("user.email.AlreadyRegistered"));
    }

    var existsWithMobilePhoneNumber = loadUser.loadUserByMobilePhoneNumber(mobilePhoneNumber);
    if (existsWithMobilePhoneNumber.isPresent()) {
      throw new BadRequestException("User already exists with phone number " + mobilePhoneNumber)
          .withViolationCodes(List.of("user.mobilePhoneNumber.AlreadyRegistered"));
    }

    var user = UserMapper.INSTANCE.mapToEntity(input);
    if (!user.getRoles().contains(Role.USER)) {
      user.getRoles().add(Role.USER); // making sure admins are also users
    }
    var saved = persistUser.save(user);

    verifyUser.sendEmailVerificationCode(saved.getId());

    // sync user with identity provider (keycloak,okta,...)
    syncIdentity.create(saved, input.password());

    return saved;
  }

  @Builder
  public record RegisterUserInput(
      @NotEmpty(message = "user.firstName.NotEmpty")
      @Size(max = 255, message = "user.firstName.Size")
      String firstName,

      @NotEmpty(message = "user.lastName.NotEmpty")
      @Size(max = 255, message = "user.lastName.Size")
      String lastName,

      @NotEmpty(message = "user.email.NotEmpty")
      @Size(max = 150, message = "user.email.Size")
      @Email(message = "user.email.Invalid")
      String email,

      @NotEmpty(message = "user.mobilePhoneNumber.NotEmpty")
      @Size(max = 150, message = "user.mobilePhoneNumber.Size")
      @MobilePhoneNumber(message = "user.mobilePhoneNumber.Invalid")
      String mobilePhoneNumber,

      @NotEmpty(message = "user.password.NotEmpty")
      @Size(max = 150, message = "user.password.Size")
      String password,

      @NotEmpty(message = "user.timeZoneId.NotEmpty")
      String timeZoneId,

      @NotEmpty(message = "user.roles.NotEmpty")
      List<Role> roles
  ) {}

}
