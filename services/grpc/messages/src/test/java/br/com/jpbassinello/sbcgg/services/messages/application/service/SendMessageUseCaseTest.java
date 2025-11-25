package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.PersistMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.SendEmailPort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.SendSmsPort;
import br.com.jpbassinello.sbcgg.services.messages.config.MessageServiceConfigProperties;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendMessageUseCaseTest {

  @Mock
  private SendEmailPort sendEmailPort;
  @Mock
  private SendSmsPort sendSmsPort;
  @Mock
  private MessageServiceConfigProperties properties;
  @Mock
  private PersistMessagePort persistMessagePort;
  @Mock
  private LoadMessagePort loadMessagePort;
  @Mock
  private LoadUserPort loadUser;
  @Mock
  private TimeNow timeNow;
  private SendMessageUseCase service;

  @BeforeEach
  void init() {
    when(sendEmailPort.getMessageType()).thenCallRealMethod();
    when(sendSmsPort.getMessageType()).thenCallRealMethod();
    service = new SendMessageUseCase(List.of(sendEmailPort, sendSmsPort),
        persistMessagePort, loadMessagePort, properties, loadUser, timeNow);
  }

  @Test
  void sendSms() {

    var now = ZonedDateTime.now();
    when(timeNow.get()).thenReturn(now);

    var userId = mockLoadUser();

    var message = buildPendingMessage(userId, MessageChannel.SMS, now);

    when(loadMessagePort.findById(message.getId())).thenReturn(Optional.of(message));

    service.send(message.getId());

    verify(sendEmailPort, never()).send(any());
    verify(sendSmsPort).send(message);

    var expected = buildPendingMessage(userId, MessageChannel.SMS, now);
    expected.setSentAt(now);
    expected.setRecipient("+5519991038010");
    expected.setStatus(MessageStatus.SENT);
    verify(persistMessagePort).save(expected);
  }

  @Test
  void sendEmail() {

    var now = ZonedDateTime.now();
    when(timeNow.get()).thenReturn(now);

    var userId = mockLoadUser();

    var message = buildPendingMessage(userId, MessageChannel.EMAIL, now);

    when(loadMessagePort.findById(message.getId())).thenReturn(Optional.of(message));

    service.send(message.getId());

    verify(sendSmsPort, never()).send(any());
    verify(sendEmailPort).send(message);

    var expected = buildPendingMessage(userId, MessageChannel.EMAIL, now);
    expected.setSentAt(now);
    expected.setRecipient("email@email.com");
    expected.setStatus(MessageStatus.SENT);
    verify(persistMessagePort).save(expected);
  }

  @Test
  void sendMaxRetriesReached() {
    var now = ZonedDateTime.now();

    var userId = mockLoadUser();

    var message = buildPendingMessage(userId, MessageChannel.EMAIL, now);

    when(loadMessagePort.findById(message.getId())).thenReturn(Optional.of(message));

    doThrow(new RuntimeException("email sender internal exception"))
        .when(sendEmailPort).send(message);

    when(properties.getMaxRetries()).thenReturn(3);

    service.send(message.getId());

    verify(sendSmsPort, never()).send(any());
    verify(sendEmailPort).send(message);

    var expected = buildPendingMessage(userId, MessageChannel.EMAIL, now);
    expected.setStatus(MessageStatus.CANCELLED_MAX_RETRY_ATTEMPTS);
    verify(persistMessagePort).save(message);
  }

  private static Message buildPendingMessage(UUID userId, MessageChannel channel, ZonedDateTime now) {
    return Message.builder()
        .userId(userId)
        .channel(channel)
        .template(MessageTemplate.OPEN_BODY)
        .status(MessageStatus.PENDING)
        .idempotenceKey("idempotenceKey")
        .registeredAt(now)
        .scheduledAt(now)
        .variables(Map.of("body", "this is a body for a message!"))
        .nextAttemptAt(now)
        .build();
  }

  private UUID mockLoadUser() {
    var userId = UUID.randomUUID();
    var user = User.builder()
        .id(userId)
        .email("email@email.com")
        .emailVerified(true)
        .mobilePhoneNumber("+5519991038010")
        .mobilePhoneNumberVerified(true)
        .firstName("John")
        .lastName("Doe")
        .build();
    when(loadUser.loadUserById(user.getId())).thenReturn(Optional.of(user));
    return userId;
  }
}
