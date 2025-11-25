package br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.BaseEntity;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "USER_VERIFICATION_CODE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class UserVerificationCode extends BaseEntity {

  @NotNull
  @Column(name = "USER_ID")
  private UUID userId;

  // phone number, email address
  @NotEmpty
  @Column(name = "SOURCE")
  private String source;

  @NotEmpty
  @Size(max = 6)
  @Column(name = "CODE")
  private String code;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "TYPE")
  private UserVerificationCodeType type;

  @NotNull
  @Column(name = "VALID_UNTIL")
  private ZonedDateTime validUntil;

  @Column(name = "CONFIRMED_AT")
  @Setter
  private ZonedDateTime confirmedAt;
}
