package br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.BaseEntity;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import br.com.jpbassinello.sbcgg.validation.MobilePhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class User extends BaseEntity {

  @NotEmpty(message = "user.firstName.NotEmpty")
  @Size(max = 255, message = "user.firstName.Size")
  @Column(name = "FIRST_NAME")
  @Setter
  private String firstName;

  @NotEmpty(message = "user.lastName.NotEmpty")
  @Size(max = 255, message = "user.lastName.Size")
  @Column(name = "LAST_NAME")
  @Setter
  private String lastName;

  @NotEmpty(message = "user.email.NotEmpty")
  @Size(max = 150, message = "user.email.Size")
  @Column(name = "EMAIL")
  @Email(message = "user.email.Invalid")
  private String email;

  @Column(name = "EMAIL_VERIFIED")
  @Setter
  private boolean emailVerified;

  @NotEmpty(message = "user.mobilePhoneNumber.NotEmpty")
  @Size(max = 150, message = "user.mobilePhoneNumber.Size")
  @Column(name = "MOBILE_PHONE_NUMBER")
  @MobilePhoneNumber(message = "user.mobilePhoneNumber.Invalid")
  private String mobilePhoneNumber;

  @Column(name = "MOBILE_PHONE_NUMBER_VERIFIED")
  @Setter
  private boolean mobilePhoneNumberVerified;

  @Column(name = "ACTIVE")
  @Setter
  private boolean active;

  @NotNull
  @Column(name = "REGISTERED_AT")
  @Setter
  private ZonedDateTime registeredAt;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = Role.class)
  @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"))
  @Column(name = "ROLE", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @NotEmpty(message = "user.roles.NotEmpty")
  private List<Role> roles = new ArrayList<>();

  @NotEmpty(message = "user.timeZoneId.NotEmpty")
  @Column(name = "TIME_ZONE_ID")
  @Setter
  @Builder.Default
  private String timeZoneId = "America/Sao_Paulo";

  public String getName() {
    return firstName + " " + lastName;
  }
}
