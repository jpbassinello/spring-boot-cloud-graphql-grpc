package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.messages;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageChannel;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessageTemplate;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageInput;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageResponse;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageGrpcAdapterTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final ZonedDateTime NOW = ZonedDateTime.parse("2023-01-01T16:35:54Z");

  @Mock
  private MessagesServiceGrpc.MessagesServiceBlockingStub stub;
  @Mock
  private TimeNow timeNow;

  @InjectMocks
  private MessageGrpcAdapter adapter;

  @Test
  void sendCodeVerificationMessage() {
    when(stub.sendMessage(any())).thenReturn(SendMessageResponse.newBuilder().build());
    when(timeNow.get()).thenReturn(NOW);
    adapter.sendVerificationCodeMessage(USER_ID, "1234");
    verify(stub).sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(USER_ID.toString())
                    .setChannel(
                        MessageChannel.MESSAGE_CHANNEL_EMAIL
                    )
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_CODE_VERIFICATION)
                    .setScheduledAt(Timestamp.newBuilder().setSeconds(1672590954).build())
                    .setIdempotenceKey(
                        "code_verification::" + USER_ID + "::2023-01-01T16:35Z"
                    )
                    .putAdditionalVariables("code", "1234")
                    .build()
            )
            .build()
    );
  }

  @Test
  void sendForgotPasswordMessage() {
    when(stub.sendMessage(any())).thenReturn(SendMessageResponse.newBuilder().build());
    when(timeNow.get()).thenReturn(NOW);
    adapter.sendForgotPasswordMessage(USER_ID, "1234");
    verify(stub).sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(USER_ID.toString())
                    .setChannel(
                        MessageChannel.MESSAGE_CHANNEL_EMAIL
                    )
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_FORGOT_PASSWORD)
                    .setScheduledAt(Timestamp.newBuilder().setSeconds(1672590954).build())
                    .setIdempotenceKey(
                        "forgot_password::" + USER_ID + "::2023-01-01T16:35Z"
                    )
                    .putAdditionalVariables("code", "1234")
                    .build()
            )
            .build()
    );
  }

  @Test
  void sendPasswordChangedMessage() {
    when(stub.sendMessage(any())).thenReturn(SendMessageResponse.newBuilder().build());
    when(timeNow.get()).thenReturn(NOW);
    adapter.sendPasswordChangedMessage(USER_ID);
    verify(stub).sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(USER_ID.toString())
                    .setChannel(
                        MessageChannel.MESSAGE_CHANNEL_EMAIL
                    )
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_PASSWORD_CHANGED)
                    .setScheduledAt(Timestamp.newBuilder().setSeconds(1672590954).build())
                    .setIdempotenceKey(
                        "password_changed::" + USER_ID + "::2023-01-01T16:35Z"
                    )
                    .build()
            )
            .build()
    );
  }

  @Test
  void sendWelcomeMessage() {
    when(stub.sendMessage(any())).thenReturn(SendMessageResponse.newBuilder().build());
    when(timeNow.get()).thenReturn(NOW);
    adapter.sendWelcomeMessage(USER_ID);
    verify(stub).sendMessage(
        SendMessageRequest.newBuilder()
            .setInput(
                SendMessageInput.newBuilder()
                    .setUserId(USER_ID.toString())
                    .setChannel(
                        MessageChannel.MESSAGE_CHANNEL_SMS
                    )
                    .setTemplate(MessageTemplate.MESSAGE_TEMPLATE_WELCOME)
                    .setScheduledAt(Timestamp.newBuilder().setSeconds(1672590954).build())
                    .setIdempotenceKey(
                        "welcome::" + USER_ID
                    )
                    .build()
            )
            .build()
    );
  }

}
