package br.com.jpbassinello.sbcgg.services.messages.adapter.in;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SearchMessagesResponse;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageRequest;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.SendMessageResponse;
import br.com.jpbassinello.sbcgg.services.messages.application.service.LoadMessagesUseCase;
import br.com.jpbassinello.sbcgg.services.messages.application.service.ScheduleMessagesUseCase;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
class MessagesGrpcAdapter extends MessagesServiceGrpc.MessagesServiceImplBase {

  private final LoadMessagesUseCase loadMessages;
  private final ScheduleMessagesUseCase scheduleMessages;

  @Override
  public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {

    var scheduledMessage = scheduleMessages.schedule(
        MessagesGrpcMapper.INSTANCE.mapToInput(request.getInput())
    );

    responseObserver.onNext(
        SendMessageResponse
            .newBuilder()
            .setMessage(MessagesGrpcMapper.INSTANCE.mapToProto(scheduledMessage))
            .build()
    );
    responseObserver.onCompleted();
  }

  @Override
  public void searchMessages(SearchMessagesRequest request, StreamObserver<SearchMessagesResponse> responseObserver) {
    var result = loadMessages.search(UUID.fromString(request.getUserId()), request.getPage(), request.getPageSize());

    var proto = SearchMessagesResponse.newBuilder()
        .setHasNext(result.hasNext())
        .addAllMessages(result.items().stream().map(MessagesGrpcMapper.INSTANCE::mapToProto).toList());

    responseObserver.onNext(proto.build());
    responseObserver.onCompleted();
  }
}
