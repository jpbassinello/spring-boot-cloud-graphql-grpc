package br.com.jpbassinello.sbcgg.services.messages.application.port.out;

import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface PersistMessagePort {

  Message save(Message message);

}
