package br.com.jpbassinello.sbcgg.services.messages.domain.enums;

public enum MessageStatus {
  PENDING,
  SENT,
  CANCELLED_MAX_RETRY_ATTEMPTS,
  CANCELLED_RECIPIENT_NOT_VERIFIED,
  CANCELLED_UNEXPECTED
}
