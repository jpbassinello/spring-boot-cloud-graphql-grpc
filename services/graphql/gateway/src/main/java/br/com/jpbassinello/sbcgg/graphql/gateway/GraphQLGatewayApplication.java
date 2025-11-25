package br.com.jpbassinello.sbcgg.graphql.gateway;

import br.com.jpbassinello.sbcgg.grpc.client.config.GrpcClientConfig;
import br.com.jpbassinello.sbcgg.spring.SpringApp;
import br.com.jpbassinello.sbcgg.spring.SpringAppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({
    GrpcClientConfig.class,
    SpringAppConfig.class
})
public class GraphQLGatewayApplication {

  static void main(String[] args) {
    SpringApp.run(GraphQLGatewayApplication.class, args);
  }

}
