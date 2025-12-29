package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.PersistMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.SendMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.config.MessageServiceConfigProperties;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
class SendMessageUseCase {

  private static final Set<MessageTemplate> BYPASS_RECIPIENT_VERIFICATION = EnumSet.of(
      MessageTemplate.CODE_VERIFICATION,
      MessageTemplate.FORGOT_PASSWORD,
      MessageTemplate.PASSWORD_CHANGED
  );
  private final Map<MessageChannel, SendMessagePort> senderByChannel;
  private final PersistMessagePort persistMessage;
  private final LoadMessagePort loadMessagePort;
  private final MessageServiceConfigProperties properties;
  private final LoadUserPort loadUser;
  private final TimeNow timeNow;

  public SendMessageUseCase(List<SendMessagePort> senders, PersistMessagePort persistMessage,
                            LoadMessagePort loadMessagePort, MessageServiceConfigProperties properties,
                            LoadUserPort loadUser, TimeNow timeNow) {
    senderByChannel = senders.stream()
        .collect(Collectors.toMap(SendMessagePort::getMessageType, Function.identity()));
    this.persistMessage = persistMessage;
    this.loadMessagePort = loadMessagePort;
    this.properties = properties;
    this.loadUser = loadUser;
    this.timeNow = timeNow;
  }

  private void cancel(Message message, MessageStatus status, String subReason) {
    log.warn("Cancelling message reason={} sub_reason={} user_id={} message_id={}",
        status, subReason, message.getUserId(), message.getId());
    message.setStatus(status);
    persistMessage.save(message);
  }

  @Nullable
  private String getRecipient(MessageChannel channel, User user) {
    return switch (channel) {
      case EMAIL -> user.getEmail();
      case SMS -> user.getMobilePhoneNumber();
    };
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  void send(UUID messageId) {

    var message = loadMessagePort.findById(messageId)
        .orElseThrow();

    var sender = senderByChannel.get(message.getChannel());

    var userExist = loadUser.loadUserById(message.getUserId());
    if (userExist.isEmpty()) {
      cancel(message, MessageStatus.CANCELLED_UNEXPECTED, "user_not_found");
      return;
    }
    var user = userExist.get();

    if (!BYPASS_RECIPIENT_VERIFICATION.contains(message.getTemplate())) {
      // need to check if recipient is already verified by the user
      if (
          (message.getChannel() == MessageChannel.SMS && !user.isMobilePhoneNumberVerified())
              || (message.getChannel() == MessageChannel.EMAIL && !user.isEmailVerified())
      ) {
        cancel(message, MessageStatus.CANCELLED_RECIPIENT_NOT_VERIFIED,
            message.getChannel().name().toLowerCase() + "_not_verified");
        return;
      }
    }

    var recipient = getRecipient(message.getChannel(), user);

    try {
      log.info("Sending message id={} to user_id={} channel={} template={} recipient={}",
          message.getId(), message.getUserId(), message.getChannel(), message.getTemplate(), recipient);
      sender.send(message);
      message.setRecipient(recipient);
      message.setStatus(MessageStatus.SENT);
      message.setSentAt(timeNow.get());
      persistMessage.save(message);
    } catch (Exception e) {
      handleSendMessageException(e, message);
    }
  }

  private void handleSendMessageException(Exception e, Message message) {
    log.warn("Failed to send message id={} to user_id={} channel={} template={}",
        message.getId(), message.getUserId(), message.getChannel(), message.getTemplate(), e);

    var retries = message.getRetries() + 1;
    message.setRetries(retries);

    if (retries > properties.getMaxRetries()) {
      message.setStatus(MessageStatus.CANCELLED_MAX_RETRY_ATTEMPTS);
    } else {
      message.setNextAttemptAt(
          message.getNextAttemptAt().plus(properties.getNextAttemptWaitDuration())
      );
    }
    persistMessage.save(message);
  }

}
