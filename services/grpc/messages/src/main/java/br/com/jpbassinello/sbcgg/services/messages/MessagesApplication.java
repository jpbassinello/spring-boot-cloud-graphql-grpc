package br.com.jpbassinello.sbcgg.services.messages;

import br.com.jpbassinello.sbcgg.cache.config.RedisConfig;
import br.com.jpbassinello.sbcgg.grpc.server.config.GrpcServerConfig;
import br.com.jpbassinello.sbcgg.services.messages.config.MessageServiceConfigProperties;
import br.com.jpbassinello.sbcgg.shedlock.config.ShedLockConfig;
import br.com.jpbassinello.sbcgg.spring.SpringApp;
import br.com.jpbassinello.sbcgg.spring.SpringAppConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import({
    GrpcServerConfig.class,
    RedisConfig.class,
    ShedLockConfig.class,
    SpringAppConfig.class
})
@EnableConfigurationProperties(MessageServiceConfigProperties.class)
@EnableScheduling
@EnableAsync
public class MessagesApplication {

  static void main(String[] args) {
    SpringApp.run(MessagesApplication.class, args);
  }

}
