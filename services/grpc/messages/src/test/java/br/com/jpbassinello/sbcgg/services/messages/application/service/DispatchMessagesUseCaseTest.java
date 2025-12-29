package br.com.jpbassinello.sbcgg.services.messages.application.service;

import br.com.jpbassinello.sbcgg.services.messages.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.spring.TimeNow;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class DispatchMessagesUseCaseTest {

  @Mock
  private SendMessageUseCase sendMessages;
  @Mock
  private LoadMessagePort loadMessages;
  @Mock
  private TimeNow timeNow;
  @InjectMocks
  private DispatchMessagesUseCase service;

  @Test
  void dispatchAll() {

    var now = ZonedDateTime.now();
    Mockito.when(timeNow.get()).thenReturn(now);

    var messageId1 = UUID.randomUUID();
    var messageId2 = UUID.randomUUID();

    Mockito.when(loadMessages.findNotSent(now)).thenReturn(
        Stream.of(
            messageId1,
            messageId2
        )
    );

    service.dispatchAll();

    var captor = ArgumentCaptor.forClass(UUID.class);

    Mockito.verify(sendMessages, Mockito.times(2)).send(captor.capture());

    Assertions.assertThat(captor.getAllValues()).usingRecursiveComparison().ignoringCollectionOrder()
        .isEqualTo(
            List.of(
                messageId1,
                messageId2
            )
        );
  }
}
