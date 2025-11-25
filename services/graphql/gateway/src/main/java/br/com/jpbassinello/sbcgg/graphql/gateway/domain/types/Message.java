package br.com.jpbassinello.sbcgg.graphql.gateway.domain.types;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageTemplate;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.UUID;

@Builder
public record Message(
    UUID id,
    UUID userId,
    MessageChannel channel,
    MessageTemplate template,
    String recipient,
    ZonedDateTime registeredAt,
    ZonedDateTime sentAt,
    MessageStatus status
) {}
