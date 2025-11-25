package br.com.jpbassinello.sbcgg.services.messages.application.port.out;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public interface LoadMessagePort {

  SimplePage<Message> searchByUserId(UUID userId, int page, int pageSize);

  Optional<Message> findByIdempotenceKeyAndChannel(String idempotenceKey, MessageChannel channel);

  Stream<UUID> findNotSent(ZonedDateTime nextAttemptBefore);

  Optional<Message> findById(UUID id);
}
