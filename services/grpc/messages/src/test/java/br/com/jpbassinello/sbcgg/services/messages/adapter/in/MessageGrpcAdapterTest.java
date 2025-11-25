package br.com.jpbassinello.sbcgg.services.messages.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageStatus;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageResponse;
import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.mapstruct.BaseProtobufMapper;
import br.com.jpbassinello.sbcgg.services.messages.application.service.LoadMessagesUseCase;
import br.com.jpbassinello.sbcgg.services.messages.application.service.ScheduleMessagesUseCase;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageTemplate;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageGrpcAdapterTest {

  @Mock
  private LoadMessagesUseCase loadMessages;
  @Mock
  private ScheduleMessagesUseCase scheduleMessages;

  @InjectMocks
  private MessagesGrpcAdapter adapter;

  @Test
  void sendMessage() {
    var now = ZonedDateTime.now();
    var nowAsProtoTimestamp = BaseProtobufMapper.mapZonedDateTimeToTimestamp(now);
    var userId = UUID.randomUUID();
    var request = SendMessageRequest.newBuilder()
        .setInput(
            SendMessageInput.newBuilder()
                .setUserId(userId.toString())
                .setTemplate(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate.MESSAGE_TEMPLATE_PASSWORD_CHANGED)
                .setScheduledAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now))
                .setChannel(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel.MESSAGE_CHANNEL_EMAIL)
                .setIdempotenceKey("key")
                .build()
        ).build();

    var id1 = UUID.randomUUID();
    when(scheduleMessages.schedule(
            ScheduleMessagesUseCase.ScheduleMessageInput.builder()
                .userId(userId)
                .template(MessageTemplate.PASSWORD_CHANGED)
                .scheduledAt(now)
                .channel(MessageChannel.EMAIL)
                .idempotenceKey("key")
                .build()
        )
    ).thenReturn(
        Message.builder()
            .userId(userId)
            .template(MessageTemplate.PASSWORD_CHANGED)
            .idempotenceKey("key")
            .registeredAt(now)
            .scheduledAt(now)
            .variables(Map.of("body", "this is a body"))
            .nextAttemptAt(now)
            .id(id1)
            .channel(MessageChannel.SMS)
            .recipient("+5519991038010")
            .build()
    );

    var captor = ArgumentCaptor.forClass(SendMessageResponse.class);
    StreamObserver<SendMessageResponse> observer = mock(StreamObserver.class);

    adapter.sendMessage(request, observer);

    verify(observer).onNext(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(
            SendMessageResponse.newBuilder()
                .setMessage(
                    br.com.jpbassinello.sbcgg.grpc.interfaces.messages.Message.newBuilder()
                        .setId(id1.toString())
                        .setUserId(userId.toString())
                        .setChannel(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel.MESSAGE_CHANNEL_SMS)
                        .setTemplate(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate.MESSAGE_TEMPLATE_PASSWORD_CHANGED)
                        .setRecipient("+5519991038010")
                        .setScheduledAt(nowAsProtoTimestamp)
                        .setRegisteredAt(nowAsProtoTimestamp)
                        .setStatus(MessageStatus.MESSAGE_STATUS_PENDING)
                        .build()
                )
                .build()
        );
  }

  @Test
  void searchMessages() {
    var now = ZonedDateTime.now();
    var id = UUID.randomUUID();
    var userId = UUID.randomUUID();
    when(loadMessages.search(userId, 0, 10))
        .thenReturn(
            new SimplePage<>(
                List.of(
                    Message.builder()
                        .id(id)
                        .userId(userId)
                        .channel(MessageChannel.SMS)
                        .template(MessageTemplate.OPEN_BODY)
                        .recipient("+5519991038010")
                        .idempotenceKey("key")
                        .registeredAt(now)
                        .scheduledAt(now.plusMinutes(1))
                        .variables(Map.of("body", "this is a body for a SMS message!"))
                        .nextAttemptAt(now)
                        .status(br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageStatus.SENT)
                        .sentAt(now.plusMinutes(2))
                        .build()
                ), true
            )
        );

    var captor = ArgumentCaptor.forClass(SearchMessagesResponse.class);
    StreamObserver<SearchMessagesResponse> observer = mock(StreamObserver.class);

    adapter.searchMessages(
        SearchMessagesRequest.newBuilder()
            .setUserId(userId.toString())
            .setPage(0)
            .setPageSize(10)
            .build(), observer
    );

    verify(observer).onNext(captor.capture());

    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(
            SearchMessagesResponse.newBuilder()
                .setHasNext(true)
                .addMessages(
                    br.com.jpbassinello.sbcgg.grpc.interfaces.messages.Message.newBuilder()
                        .setId(id.toString())
                        .setUserId(userId.toString())
                        .setChannel(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel.MESSAGE_CHANNEL_SMS)
                        .setTemplate(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate.MESSAGE_TEMPLATE_OPEN_BODY)
                        .setRecipient("+5519991038010")
                        .setScheduledAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now.plusMinutes(1)))
                        .setRegisteredAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now))
                        .setStatus(MessageStatus.MESSAGE_STATUS_SENT)
                        .setSentAt(BaseProtobufMapper.mapZonedDateTimeToTimestamp(now.plusMinutes(2)))
                        .build()
                )
                .build()
        );

  }

}
