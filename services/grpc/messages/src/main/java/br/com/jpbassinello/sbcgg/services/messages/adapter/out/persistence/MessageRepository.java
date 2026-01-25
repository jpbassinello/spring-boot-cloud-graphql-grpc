package br.com.jpbassinello.sbcgg.services.messages.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus;
import com.google.errorprone.annotations.MustBeClosed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findAllByUserId(UUID userId, Pageable pageAndSort);

  Optional<Message> findByIdempotenceKeyAndChannel(String idempotenceKey, MessageChannel channel);

  @Query("SELECT m.id FROM Message m WHERE m.status = :status and m.nextAttemptAt <= :attemptAtBefore")
  @MustBeClosed
  Stream<UUID> findAllByStatusAndNextAttemptAtBefore(@Param("status") MessageStatus status, @Param("attemptAtBefore") ZonedDateTime attemptAtBefore);
}
