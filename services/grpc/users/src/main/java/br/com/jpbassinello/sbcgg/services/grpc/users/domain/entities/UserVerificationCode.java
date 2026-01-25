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
@Table(name = "user_verification_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class UserVerificationCode extends BaseEntity {

  @NotNull
  @Column(name = "user_id")
  private UUID userId;

  // phone number, email address
  @NotEmpty
  @Column(name = "source")
  private String source;

  @NotEmpty
  @Size(max = 6)
  @Column(name = "code")
  private String code;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private UserVerificationCodeType type;

  @NotNull
  @Column(name = "valid_until")
  private ZonedDateTime validUntil;

  @Column(name = "confirmed_at")
  @Setter
  private ZonedDateTime confirmedAt;
}
