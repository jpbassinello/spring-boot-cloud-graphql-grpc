package br.com.jpbassinello.sbcgg.graphql.gateway.adapter.in;

import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadMessagePort;
import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.RegisterUserPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.application.port.out.VerifyUserContactMethodPort;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageChannel;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageStatus;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.MessageTemplate;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.Message;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserMessagePage;
import br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.UserPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class UserQueryIT {

  @MockitoBean
  private LoadMessagePort loadMessagePort;
  @MockitoBean
  private LoadUserPort loadUserPort;
  @MockitoBean
  private RegisterUserPort registerUserPort;
  @MockitoBean
  private VerifyUserContactMethodPort verifyUserContactMethodPort;

  @Autowired
  private ExecutionGraphQlService graphQlService;

  private ExecutionGraphQlServiceTester graphQlTester;

  @BeforeEach
  void setUp() {
    graphQlTester = ExecutionGraphQlServiceTester.create(graphQlService);
  }

  @Test
  void shouldRescheduleSecondPayment() {
    var userId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    var user = User.builder()
        .id(userId)
        .email("john.doe@email.com")
        .firstName("John")
        .lastName("Doe")
        .build();
    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.of(user));

    var now = ZonedDateTime.parse("2021-05-01T10:01:01.000Z");
    var message = Message.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .channel(MessageChannel.SMS)
        .template(MessageTemplate.OPEN_BODY)
        .recipient("+5519991038010")
        .registeredAt(now)
        .sentAt(now.plusDays(1))
        .status(MessageStatus.SENT)
        .build();
    when(loadMessagePort.searchMessages(userId, 0)).thenReturn(
        new UserMessagePage(List.of(message), false
        )
    );

    // When - Query user by ID
    var graphQLQuery = """
        query {
          user(id: "00000000-0000-0000-0000-000000000000") {
            id
            email
            firstName
            lastName
            messages(page: 0) {
              hasNext
              messages {
                id
                channel
                template
                recipient
                registeredAt
                sentAt
                status
              }
            }
          }
        }
        """;

    graphQlTester.document(graphQLQuery)
        .execute()
        .path("user.id").entity(UUID.class).isEqualTo(userId)
        .path("user.email").entity(String.class).isEqualTo("john.doe@email.com")
        .path("user.messages.messages[0].id").entity(UUID.class).isEqualTo(message.id())
        .path("user.messages.messages[0].sentAt").entity(String.class).isEqualTo("2021-05-02T10:01:01.000Z");

    when(loadUserPort.search("john", 0, 10))
        .thenReturn(new UserPage(List.of(user), false));

    var graphQLQueryForSearch = """
        query {
          users(input: {terms: "john", page: 0, pageSize: 10}) {
            users {
              id
            }
            hasNext
          }
        }
        """;

    graphQlTester.document(graphQLQueryForSearch)
        .execute()
        .path("users.users[0].id").entity(UUID.class).isEqualTo(userId)
        .path("users.hasNext").entity(Boolean.class).isEqualTo(false);
  }
}
