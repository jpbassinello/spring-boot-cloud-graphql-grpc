package br.com.jpbassinello.sbcgg.services.messages.application.port.out;

import br.com.jpbassinello.sbcgg.services.messages.domain.enums.MessageChannel;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SendEmailPort extends SendMessagePort {

  @Override
  default MessageChannel getMessageType() {
    return MessageChannel.EMAIL;
  }
}
