package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.services.messages.domain.entities.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class LoadMessagesUseCase {

  private final LoadMessagePort loadMessages;

  public SimplePage<Message> search(UUID userId, int page, int pageSize) {
    return loadMessages.searchByUserId(userId, page, pageSize);
  }
}
