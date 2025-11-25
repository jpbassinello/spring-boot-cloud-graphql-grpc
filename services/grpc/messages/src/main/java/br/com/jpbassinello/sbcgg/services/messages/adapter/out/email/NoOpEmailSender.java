package br.com.jpbassinello.sbcgg.services.messages.adapter.out.email;

import br.com.jpbassinello.sbcgg.services.messages.application.port.out.SendEmailPort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@Component
@Slf4j
@ParametersAreNonnullByDefault
class NoOpEmailSender implements SendEmailPort {

  @Override
  public void send(Message message) {
    log.info("NoOpEmailSender simulating send message id={} user_id={} recipient={} channel={} template={} variables={}",
        message.getId(),
        message.getUserId(),
        message.getRecipient(),
        message.getChannel(),
        message.getTemplate(),
        message.getVariables()
    );
  }
}
