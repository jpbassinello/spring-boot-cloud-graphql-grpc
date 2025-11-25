package br.com.jpbassinello.sbcgg.services.grpc.users;

import br.com.jpbassinello.sbcgg.grpc.server.config.GrpcServerConfig;
import br.com.jpbassinello.sbcgg.spring.SpringApp;
import br.com.jpbassinello.sbcgg.spring.SpringAppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
    GrpcServerConfig.class,
    SpringAppConfig.class
})
public class UsersApplication {

  static void main(String[] args) {
    SpringApp.run(UsersApplication.class, args);
  }

}
