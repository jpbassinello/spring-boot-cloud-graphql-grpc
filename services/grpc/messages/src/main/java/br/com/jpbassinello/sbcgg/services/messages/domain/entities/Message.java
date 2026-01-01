package br.com.jpbassinello.sbcgg.services.messages.domain.entities;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.BaseEntity;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Message extends BaseEntity {

  @NotNull(message = "message.userId.NotNull")
  @Column(name = "user_id")
  private UUID userId;

  @NotNull(message = "message.type.NotNull")
  @Column(name = "channel")
  @Enumerated(EnumType.STRING)
  @Setter
  private MessageChannel channel;

  @NotNull(message = "message.template.NotNull")
  @Column(name = "template")
  @Enumerated(EnumType.STRING)
  private MessageTemplate template;

  // email or phone number
  @Size(max = 150, message = "message.recipient.Size")
  @Column(name = "recipient")
  @Setter
  private String recipient;

  @NotEmpty
  @Column(name = "idempotence_key")
  private String idempotenceKey;

  @NotNull
  @Column(name = "registered_at")
  @Builder.Default
  private ZonedDateTime registeredAt = ZonedDateTime.now();

  @NotNull(message = "message.scheduledAt.NotNull")
  @Column(name = "scheduled_at")
  private ZonedDateTime scheduledAt;

  @Column(name = "sent_at")
  @Setter
  private ZonedDateTime sentAt;

  @NotNull
  @Column(name = "next_attempt_at")
  @Setter
  private ZonedDateTime nextAttemptAt;

  @Column(name = "retries")
  @Builder.Default
  @Setter
  private int retries = 0;

  @NotNull(message = "message.status.NotNull")
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Setter
  private MessageStatus status = MessageStatus.PENDING;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "message_variable",
      joinColumns = @JoinColumn(name = "message_id", referencedColumnName = "id"))
  @MapKeyColumn(name = "key", length = 100)
  @Column(name = "value", nullable = false, length = 1000)
  @Builder.Default
  private Map<String, String> variables = new HashMap<>();
}
