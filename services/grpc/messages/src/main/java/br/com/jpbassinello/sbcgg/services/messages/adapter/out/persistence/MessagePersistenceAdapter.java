package br.com.jpbassinello.sbcgg.services.messages.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.PersistMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
class MessagePersistenceAdapter implements PersistMessagePort, LoadMessagePort {

  private final MessageRepository messageRepository;

  @Override
  public Message save(Message message) {
    return messageRepository.save(message);
  }

  @Override
  public SimplePage<Message> searchByUserId(UUID userId, int page, int pageSize) {
    if (pageSize > 100) {
      throw new IllegalArgumentException("Requested to fetch more messages than permitted");
    }

    // using pageSize + 1 to check if result list size has more elements
    var results = messageRepository.findAllByUserId(userId, PageRequest.of(page, pageSize + 1));
    var hasNext = results.size() > pageSize;
    return new SimplePage<>(
        results.subList(0, Math.min(results.size(), pageSize)), // only returning what is expected in the page
        hasNext
    );
  }

  @Override
  public Optional<Message> findByIdempotenceKeyAndChannel(String idempotenceKey, MessageChannel channel) {
    return messageRepository.findByIdempotenceKeyAndChannel(idempotenceKey, channel);
  }

  @Override
  @Transactional(readOnly = true)
  public Stream<UUID> findNotSent(ZonedDateTime nextAttemptBefore) {
    return messageRepository.findAllByStatusAndNextAttemptAtBefore(MessageStatus.PENDING, nextAttemptBefore);
  }

  @Override
  public Optional<Message> findById(UUID id) {
    return messageRepository.findById(id);
  }
}
