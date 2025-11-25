package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.messages;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MessageGrpcAdapterConfig {

  @GrpcClient("messages")
  private MessagesServiceGrpc.MessagesServiceBlockingStub stub;

  @Bean
  MessagesServiceGrpc.MessagesServiceBlockingStub messagesGrpc() {
    return stub;
  }
}
