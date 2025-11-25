package br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums;

public enum MessageStatus {
  PENDING,
  SENT,
  CANCELLED_MAX_RETRY_ATTEMPTS,
  CANCELLED_RECIPIENT_NOT_VERIFIED,
  CANCELLED_UNEXPECTED
}
