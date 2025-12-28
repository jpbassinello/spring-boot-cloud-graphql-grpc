package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.out.message;

import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
class MessageGrpcAdapterConfig {

  @Bean
  MessagesServiceGrpc.MessagesServiceBlockingStub messagesGrpc(GrpcChannelFactory channels) {
    return MessagesServiceGrpc.newBlockingStub(channels.createChannel("messages"));
  }
}