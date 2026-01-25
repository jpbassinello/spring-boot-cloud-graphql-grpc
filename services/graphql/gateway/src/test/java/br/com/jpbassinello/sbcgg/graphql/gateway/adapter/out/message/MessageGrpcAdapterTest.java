package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.message;

import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserMessagePage;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageGrpcAdapterTest {

  private static final UUID USER_ID = UUID.randomUUID();

  @Mock
  private MessagesServiceGrpc.MessagesServiceBlockingStub stub;
  @Mock
  private GrpcChannelFactory channels;

  private MessageGrpcAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MessageGrpcAdapter(channels);
    ReflectionTestUtils.setField(adapter, "messagesGrpc", stub);
  }

  @Test
  void searchMessages() {
    when(stub.searchMessages(
        SearchMessagesRequest.newBuilder()
            .setUserId(USER_ID.toString())
            .setPage(2)
            .setPageSize(10)
            .build()
    )).thenReturn(
        SearchMessagesResponse.newBuilder()
            .addMessages(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.Message.newBuilder().setRecipient("email1@email.com").build())
            .addMessages(br.com.jpbassinello.sbcgg.grpc.interfaces.messages.Message.newBuilder().setRecipient("+5519991038010").build())
            .setHasNext(true)
            .build()
    );

    var response = adapter.searchMessages(USER_ID, 2);

    assertThat(response)
        .usingRecursiveComparison()
        .isEqualTo(
            new UserMessagePage(
                List.of(
                    br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.Message.builder().recipient("email1@email.com").build(),
                    br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.Message.builder().recipient("+5519991038010").build()
                ), true
            )
        );
  }

}
