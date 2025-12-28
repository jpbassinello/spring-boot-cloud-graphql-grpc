package br.com.jpbassinello.sbcgg.services.messages.adapter.out.users;

import br.com.jpbassinello.sbcgg.grpc.interfaces.users.UsersServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
class UserGrpcAdapterConfig {

  @Bean
  UsersServiceGrpc.UsersServiceBlockingStub usersGrpc(GrpcChannelFactory channels) {
    return UsersServiceGrpc.newBlockingStub(channels.createChannel("users"));
  }
}