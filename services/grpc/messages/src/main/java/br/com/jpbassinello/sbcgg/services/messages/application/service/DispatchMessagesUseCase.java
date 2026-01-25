package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
class DispatchMessagesUseCase {

  private final SendMessageUseCase sendMessages;
  private final LoadMessagePort loadMessages;
  private final TimeNow timeNow;

  @Scheduled(fixedDelay = 1000)
  @Transactional(readOnly = true)
  @SchedulerLock(name = "DispatchMessagesUseCase#dispatchAll")
  public void dispatchAll() {
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      loadMessages.findNotSent(timeNow.get())
          .forEach(id -> executor.submit(() -> sendMessages.send(id)));
    }
  }

}
