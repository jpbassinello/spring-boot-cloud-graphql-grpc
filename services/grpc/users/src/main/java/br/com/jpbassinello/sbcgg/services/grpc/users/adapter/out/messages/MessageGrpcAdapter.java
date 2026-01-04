package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.messages;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageRequest;
import br.com.jpbassinello.sbcgg.mapstruct.BaseProtobufMapper;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.SendMessagePort;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
@Slf4j
class MessageGrpcAdapter implements SendMessagePort {

  private final TimeNow timeNow;
  private final GrpcChannelFactory channels;
  private MessagesServiceGrpc.MessagesServiceBlockingStub messagesGrpc;

  private synchronized MessagesServiceGrpc.MessagesServiceBlockingStub getMessagesGrpc() {
    if (messagesGrpc == null) {
      messagesGrpc = MessagesServiceGrpc.newBlockingStub(channels.createChannel("messages"));
    }
    return messagesGrpc;
  }

  @Override
  public void sendVerificationCodeMessage(UUID userId, String code) {
    var now = timeNow.get();
    // making sure we allow one of this each minute only for a user
    var nowTruncatedToMinutes = now.truncatedTo(ChronoUnit.MINUTES);

    getMessagesGrpc().sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(userId.toString())
                    .setChannel(MessageChannel.MESSAGE_CHANNEL_EMAIL)
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_CODE_VERIFICATION)
                    .setScheduledAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now))
                    .setIdempotenceKey(
                        "%s::%s::%s".formatted("code_verification", userId, nowTruncatedToMinutes.toString())
                    )
                    .putAdditionalVariables("code", code)
                    .build()
            )
            .build()
    );

    log.info("sendCodeVerification user_id={}", userId);
  }

  @Override
  public void sendForgotPasswordMessage(UUID userId, String code) {
    var now = timeNow.get();
    // making sure we allow one of this each minute only for a user
    var nowTruncatedToMinutes = now.truncatedTo(ChronoUnit.MINUTES);

    getMessagesGrpc().sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(userId.toString())
                    .setChannel(MessageChannel.MESSAGE_CHANNEL_EMAIL)
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_FORGOT_PASSWORD)
                    .setScheduledAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now))
                    .setIdempotenceKey(
                        "%s::%s::%s".formatted("forgot_password", userId, nowTruncatedToMinutes.toString())
                    )
                    .putAdditionalVariables("code", code)
                    .build()
            )
            .build()
    );

    log.info("sendForgotPassword user_id={}", userId);
  }

  @Override
  public void sendPasswordChangedMessage(UUID userId) {
    var now = timeNow.get();
    // making sure we allow one of this each minute only for a user
    var nowTruncatedToMinutes = now.truncatedTo(ChronoUnit.MINUTES);

    getMessagesGrpc().sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(userId.toString())
                    .setChannel(MessageChannel.MESSAGE_CHANNEL_EMAIL)
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_PASSWORD_CHANGED)
                    .setScheduledAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now))
                    .setIdempotenceKey(
                        "%s::%s::%s".formatted("password_changed", userId, nowTruncatedToMinutes.toString())
                    )
                    .build()
            )
            .build()
    );

    log.info("sendPasswordChangedMessage user_id={}", userId);
  }

  @Override
  public void sendWelcomeMessage(UUID userId) {
    var inputBuilder = SendMessageInput.newBuilder()
        .setUserId(userId.toString())
        .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_WELCOME)
        .setScheduledAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(timeNow.get()))
        .setIdempotenceKey(
            "%s::%s".formatted("welcome", userId) // making sure a user receives this only once
        );

    Stream.of(MessageChannel.MESSAGE_CHANNEL_EMAIL, MessageChannel.MESSAGE_CHANNEL_SMS)
        .forEach(channel -> getMessagesGrpc().sendMessage(
            SendMessageRequest.newBuilder()
                .setInput(
                    inputBuilder
                        .setChannel(channel)
                        .build()
                )
                .build()
        ));

    log.info("sendWelcomeMessage user_id={}", userId);
  }
}
