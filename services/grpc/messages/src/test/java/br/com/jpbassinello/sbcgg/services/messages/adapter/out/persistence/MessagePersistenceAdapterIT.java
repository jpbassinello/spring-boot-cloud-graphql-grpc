package br.com.jpbassinello.sbcgg.services.messages.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.messages.config.PersistenceConfig;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.TestDatabaseAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
@ContextConfiguration(classes = {PersistenceConfig.class})
@ActiveProfiles("test")
@ImportTestcontainers(PostgresContainer.class)
class MessagePersistenceAdapterIT {

  @Autowired
  private MessageRepository repository;
  @Autowired
  private MessagePersistenceAdapter adapter;

  @Test
  void saveAndLoad() {
    var idempotenceKey1 = UUID.randomUUID().toString();
    var idempotenceKey2 = UUID.randomUUID().toString();
    var userId1 = UUID.randomUUID();
    var userId2 = UUID.randomUUID();
    var now = ZonedDateTime.now();
    var msg1 = adapter.save(
        Message.builder()
            .userId(userId1)
            .channel(MessageChannel.SMS)
            .template(MessageTemplate.OPEN_BODY)
            .recipient("+5519991038010")
            .idempotenceKey(idempotenceKey1)
            .registeredAt(now)
            .scheduledAt(now.plus(1, ChronoUnit.MILLIS))
            .variables(new HashMap<>(Map.of("body", "this is a body for a SMS message!"))) // needs to be mutable to make Hibernate happy
            .nextAttemptAt(now.plus(1, ChronoUnit.MILLIS))
            .build()
    );
    var msg2 = adapter.save(
        Message.builder()
            .userId(userId2)
            .channel(MessageChannel.EMAIL)
            .template(MessageTemplate.FORGOT_PASSWORD)
            .recipient("email@email.com")
            .idempotenceKey(idempotenceKey2)
            .registeredAt(now)
            .scheduledAt(now.plus(2, ChronoUnit.MILLIS))
            .variables(new HashMap<>(Map.of("body", "email forgot password")))  // needs to be mutable to make Hibernate happy
            .nextAttemptAt(now.plus(2, ChronoUnit.MILLIS))
            .build()
    );
    repository.flush();

    var loaded = adapter.findByIdempotenceKeyAndChannel(idempotenceKey1, MessageChannel.SMS).orElseThrow();

    assertThat(loaded.getVariables()).isEqualTo(Map.of("body", "this is a body for a SMS message!"));

    // not expecting any message using "now" as nextAttempt since they are scheduled to be executed after now
    assertThat(adapter.findNotSent(now).toList()).isEmpty();

    // expecting to find the SMS message when nextAttemptAt is in the next MILLISECOND
    assertThat(adapter.findNotSent(now.plus(2, ChronoUnit.MILLIS)).toList())
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(List.of(msg1.getId(), msg2.getId()));

    // should find both messages when using the other millisecond
    assertThat(adapter.findNotSent(now.plus(3, ChronoUnit.MILLIS)).count()).isEqualTo(2);

    // setting status and trying to reload
    loaded.setStatus(MessageStatus.SENT);
    adapter.save(loaded);
    repository.flush();
    // expected to still find the EMAIL message to be sent
    var stillPendingMessage = adapter.findByIdempotenceKeyAndChannel(idempotenceKey2, MessageChannel.EMAIL);
    assertThat(stillPendingMessage.orElseThrow().getChannel()).isEqualTo(MessageChannel.EMAIL);
    assertThat(adapter.findNotSent(now.plus(3, ChronoUnit.MILLIS)).toList())
        .usingRecursiveComparison()
        .isEqualTo(List.of(stillPendingMessage.orElseThrow().getId()));

    var searchResult = adapter.searchByUserId(userId1, 0, 10);
    assertThat(searchResult)
        .usingRecursiveComparison()
        .isEqualTo(
            new SimplePage<>(
                List.of(loaded),
                false
            )
        );
  }

}
