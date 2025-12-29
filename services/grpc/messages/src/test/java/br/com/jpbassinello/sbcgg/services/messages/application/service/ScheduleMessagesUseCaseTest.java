package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.exception.BadRequestException;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.PersistMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleMessagesUseCaseTest {

  private static final Validator VALIDATOR;

  static {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      VALIDATOR = factory.getValidator();
    }
  }

  @Mock
  private LoadMessagePort loadMessagePort;
  @Mock
  private PersistMessagePort persistMessagePort;
  @InjectMocks
  private ScheduleMessagesUseCase service;

  @Test
  void validate() {

    var invalidInput = ScheduleMessagesUseCase.ScheduleMessageInput.builder()
        .userId(UUID.randomUUID())
        .channel(MessageChannel.EMAIL)
        .idempotenceKey(UUID.randomUUID().toString())
        .build();

    var violations = VALIDATOR.validate(invalidInput);

    assertThat(violations)
        .hasSize(1)
        .allMatch(violation -> "message.template.NotNull".equals(violation.getMessage()));

    var validInput = ScheduleMessagesUseCase.ScheduleMessageInput.builder()
        .userId(UUID.randomUUID())
        .channel(MessageChannel.EMAIL)
        .template(MessageTemplate.CODE_VERIFICATION)
        .idempotenceKey(UUID.randomUUID().toString())
        .build();

    assertThat(VALIDATOR.validate(validInput)).isEmpty();
  }

  @Test
  void scheduleAlreadyExists() {
    var userId = UUID.randomUUID();

    var idempotenceKey = "key";
    when(loadMessagePort.findByIdempotenceKeyAndChannel(eq(idempotenceKey), any()))
        .thenReturn(Optional.of(Message.builder().build()));

    var request = ScheduleMessagesUseCase.ScheduleMessageInput.builder()
        .userId(userId)
        .idempotenceKey(idempotenceKey)
        .channel(MessageChannel.SMS)
        .build();

    assertThatThrownBy(() -> service.schedule(request))
        .isInstanceOf(BadRequestException.class)
        .matches(ex -> ((BadRequestException) ex).getViolationCodes().equals(List.of("message.AlreadySent")));
  }

  @Test
  void scheduleBadVariables() {
    var userId = UUID.randomUUID();

    var request = ScheduleMessagesUseCase.ScheduleMessageInput.builder()
        .userId(userId)
        .idempotenceKey("key")
        .template(MessageTemplate.OPEN_BODY)
        .build();

    assertThatThrownBy(() -> service.schedule(request))
        .isInstanceOf(BadRequestException.class)
        .matches(ex -> ((BadRequestException) ex).getViolationCodes().equals(List.of("message.MissingRequiredVars")));
  }

  @Test
  void schedule() {
    var userId = UUID.randomUUID();
    var idempotenceKey = "key";
    var now = ZonedDateTime.now();

    var request = ScheduleMessagesUseCase.ScheduleMessageInput.builder()
        .userId(userId)
        .channel(MessageChannel.SMS)
        .template(MessageTemplate.OPEN_BODY)
        .idempotenceKey(idempotenceKey)
        .scheduledAt(now)
        .variables(Map.of("body", "this is a body for a SMS message!"))
        .build();

    var expected = Message.builder()
        .userId(userId)
        .channel(MessageChannel.SMS)
        .template(MessageTemplate.OPEN_BODY)
        .idempotenceKey(idempotenceKey)
        .scheduledAt(now)
        .variables(Map.of("body", "this is a body for a SMS message!"))
        .nextAttemptAt(now)
        .build();

    var captor = ArgumentCaptor.forClass(Message.class);
    when(persistMessagePort.save(captor.capture())).thenAnswer(AdditionalAnswers.returnsFirstArg());

    var scheduled = service.schedule(request);

    assertThat(scheduled).usingRecursiveComparison().ignoringFields("registeredAt")
        .isEqualTo(expected);
  }
}
