package br.com.jpbassinello.sbcgg.services.grpc.users;

import br.com.jpbassinello.sbcgg.grpc.client.config.GrpcClientConfig;
import br.com.jpbassinello.sbcgg.grpc.interfaces.messages.MessagesServiceGrpc;
import br.com.jpbassinello.sbcgg.grpc.server.config.GrpcServerConfig;
import br.com.jpbassinello.sbcgg.spring.SpringApp;
import br.com.jpbassinello.sbcgg.spring.SpringAppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@Import({
    GrpcClientConfig.class,
    GrpcServerConfig.class,
    SpringAppConfig.class
})
public class UsersApplication {

  static void main(String[] args) {
    SpringApp.run(UsersApplication.class, args);
  }

}
