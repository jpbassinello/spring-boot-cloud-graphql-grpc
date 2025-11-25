package br.com.jpbassinello.sbcgg.services.messages.adapter.out.sms;

import br.com.jpbassinello.sbcgg.services.messages.application.port.out.SendSmsPort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@Component
@Slf4j
@ParametersAreNonnullByDefault
class NoOpSmsSender implements SendSmsPort {

  @Override
  public void send(Message message) {
    log.info("NoOpSmsSender simulating send message id={} user_id={} recipient={} channel={} template={} variables={}",
        message.getId(),
        message.getUserId(),
        message.getRecipient(),
        message.getChannel(),
        message.getTemplate(),
        message.getVariables()
    );
  }
}
