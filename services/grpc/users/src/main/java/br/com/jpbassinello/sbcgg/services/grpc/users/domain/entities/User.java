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
@Table(name = "users")
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString
public class User extends BaseEntity {

  @NotEmpty(message = "user.firstName.NotEmpty")
  @Size(max = 255, message = "user.firstName.Size")
  @Column(name = "first_name")
  @Setter
  private String firstName;

  @NotEmpty(message = "user.lastName.NotEmpty")
  @Size(max = 255, message = "user.lastName.Size")
  @Column(name = "last_name")
  @Setter
  private String lastName;

  @NotEmpty(message = "user.email.NotEmpty")
  @Size(max = 150, message = "user.email.Size")
  @Column(name = "email")
  @Email(message = "user.email.Invalid")
  private String email;

  @Column(name = "email_verified")
  @Setter
  private boolean emailVerified;

  @NotEmpty(message = "user.mobilePhoneNumber.NotEmpty")
  @Size(max = 150, message = "user.mobilePhoneNumber.Size")
  @Column(name = "mobile_phone_number")
  @MobilePhoneNumber(message = "user.mobilePhoneNumber.Invalid")
  private String mobilePhoneNumber;

  @Column(name = "mobile_phone_number_verified")
  @Setter
  private boolean mobilePhoneNumberVerified;

  @Column(name = "active")
  @Setter
  private boolean active;

  @NotNull
  @Column(name = "registered_at")
  @Setter
  private ZonedDateTime registeredAt;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = Role.class)
  @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "role", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @NotEmpty(message = "user.roles.NotEmpty")
  private List<Role> roles = new ArrayList<>();

  @NotEmpty(message = "user.timeZoneId.NotEmpty")
  @Column(name = "time_zone_id")
  @Setter
  @Builder.Default
  private String timeZoneId = "America/Sao_Paulo";

  public String getName() {
    return firstName + " " + lastName;
  }
}
