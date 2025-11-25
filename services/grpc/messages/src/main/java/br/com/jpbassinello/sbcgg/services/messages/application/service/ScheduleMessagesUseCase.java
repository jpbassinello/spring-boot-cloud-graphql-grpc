package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.exception.BadRequestException;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.PersistMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
@Slf4j
@Validated
public class ScheduleMessagesUseCase {

  private static final EnumMap<MessageTemplate, Set<String>> REQUIRED_VARIABLES_BY_TEMPLATE = new EnumMap<>(MessageTemplate.class);

  static {
    // fill in required variables for each template to avoid bad states for messages

    REQUIRED_VARIABLES_BY_TEMPLATE.put(
        MessageTemplate.CODE_VERIFICATION, Set.of("code") // code to be sent to the user
    );

    REQUIRED_VARIABLES_BY_TEMPLATE.put(
        MessageTemplate.OPEN_BODY, Set.of("body") // body of the message (free text)
    );

    REQUIRED_VARIABLES_BY_TEMPLATE.put(
        MessageTemplate.FORGOT_PASSWORD, Set.of("code") // code to be sent to the user
    );
  }

  private final LoadMessagePort loadMessage;
  private final PersistMessagePort persistMessage;

  private static void validateRequiredVariables(ScheduleMessageInput input) {
    var template = input.getTemplate();
    var requiredVars = REQUIRED_VARIABLES_BY_TEMPLATE.getOrDefault(template, Set.of());
    var requiredVarsNotPresent = requiredVars
        .stream()
        .filter(var -> !input.getVariables().containsKey(var))
        .collect(Collectors.joining(","));
    if (!requiredVarsNotPresent.isEmpty()) {
      log.warn("Missing required vars to send message user_id={} template={} missing_vars={}",
          input.getUserId(), template, requiredVarsNotPresent);
      throw new BadRequestException("Missing required data to be able to send message: " + requiredVarsNotPresent)
          .withViolationCodes(List.of("message.MissingRequiredVars"));
    }
  }

  @Transactional
  public Message schedule(@Valid ScheduleMessageInput input) {

    var userId = input.getUserId();

    validateRequiredVariables(input);

    var idempotenceKey = input.getIdempotenceKey();
    var channel = input.getChannel();

    log.info("Scheduling message user_id={} template={} channel={} idempotence_key={}",
        userId, input.getTemplate(), channel, idempotenceKey);

    var alreadyExist = loadMessage.findByIdempotenceKeyAndChannel(idempotenceKey, channel);
    if (alreadyExist.isPresent()) {
      throw new BadRequestException("Same message was already sent to the user")
          .withViolationCodes(List.of("message.AlreadySent"));
    }

    var message = MessageMapper.INSTANCE.mapToEntity(input);

    return persistMessage.save(message);
  }

  @Builder
  @Value
  public static class ScheduleMessageInput {
    @NotNull(message = "message.userId.NotNull")
    UUID userId;
    @NotNull(message = "message.channel.NotNull")
    MessageChannel channel;
    @NotNull(message = "message.template.NotNull")
    MessageTemplate template;
    @NotEmpty
    String idempotenceKey;
    @NotNull(message = "message.scheduledAt.NotNull")
    @Builder.Default
    ZonedDateTime scheduledAt = ZonedDateTime.now().plusSeconds(1); // just a small delay
    @Builder.Default
    Map<String, String> variables = Map.of();
  }

}
