package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.UserVerificationCode;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.UserVerificationCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
interface UserVerificationCodeRepository extends JpaRepository<UserVerificationCode, UUID> {

  Optional<UserVerificationCode> findByUserIdAndCodeAndTypeAndValidUntilGreaterThanAndConfirmedAtIsNull(
      UUID userId,
      String code,
      UserVerificationCodeType type,
      ZonedDateTime now
  );
}
