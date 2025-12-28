package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.message;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserMessagePage;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
class MessageGrpcAdapter implements LoadMessagePort {

  private final MessagesServiceGrpc.MessagesServiceBlockingStub messagesGrpc;

  @Override
  public UserMessagePage searchMessages(UUID userId, int page) {
    var response = messagesGrpc.searchMessages(
        SearchMessagesRequest.newBuilder()
            .setUserId(userId.toString())
            .setPage(page)
            .setPageSize(10)
            .build()
    );
    var messages = response.getMessagesList()
        .stream()
        .map(MessageGrpcMapper.INSTANCE::mapToType)
        .toList();

    return new UserMessagePage(
        messages,
        response.getHasNext()
    );
  }
}
