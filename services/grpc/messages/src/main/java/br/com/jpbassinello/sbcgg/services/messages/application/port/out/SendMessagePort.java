package br.com.jpbassinello.sbcgg.services.messages.application.port.out;

import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SendMessagePort {

  void send(Message message);

  MessageChannel getMessageType();
}
