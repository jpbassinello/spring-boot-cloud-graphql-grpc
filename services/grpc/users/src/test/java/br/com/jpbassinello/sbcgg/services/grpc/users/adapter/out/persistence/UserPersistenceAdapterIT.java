package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.jpa.test.PostgresContainer;
import br.com.jpbassinello.sbcgg.services.grpc.users.config.PersistenceConfig;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.TestDatabaseAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
@ContextConfiguration(classes = {PersistenceConfig.class})
@ActiveProfiles("test")
@ImportTestcontainers(PostgresContainer.class)
class UserPersistenceAdapterIT {

  @Autowired
  private UserPersistenceAdapter adapter;

  @Test
  void search() {
    adapter.save(
        User.builder()
            .firstName("João")
            .lastName("Bassinello")
            .email("email1@sbcgg.com")
            .mobilePhoneNumber("+5519991038010")
            .roles(List.of(Role.USER, Role.ADMIN))
            .build()
    );
    adapter.save(
        User.builder()
            .firstName("Pedro")
            .lastName("Bassinello")
            .email("email2@sbcgg.com")
            .mobilePhoneNumber("+5519991038011")
            .roles(List.of(Role.USER))
            .build()
    );
    adapter.save(
        User.builder()
            .firstName("Joao")
            .lastName("Batista")
            .email("email3@sbcgg.com")
            .mobilePhoneNumber("+5519991038012")
            .roles(List.of(Role.ADMIN))
            .build()
    );

    var allJoaoUsers = adapter.search("Joao", 0, 5);
    assertThat(allJoaoUsers.items()).hasSize(2);
    assertThat(allJoaoUsers.hasNext()).isFalse();

    var allJoaoUsersPageZero = adapter.search("Joao", 0, 1);
    assertThat(allJoaoUsersPageZero.items()).hasSize(1);
    assertThat(allJoaoUsersPageZero.hasNext()).isTrue();
    assertThat(allJoaoUsersPageZero.items().getFirst().getName()).isEqualTo("João Bassinello");
    var allJoaoUsersPageOne = adapter.search("Joao", 1, 1);
    assertThat(allJoaoUsersPageOne.items()).hasSize(1);
    assertThat(allJoaoUsersPageOne.hasNext()).isFalse();
    assertThat(allJoaoUsersPageOne.items().getFirst().getName()).isEqualTo("Joao Batista");

    var pedroUser = adapter.search("Pedro Bassinello", 0, 10);
    assertThat(pedroUser.items()).hasSize(1);
    assertThat(pedroUser.hasNext()).isFalse();

    var allBassinellos = adapter.search("bassinello", 0, 10);
    assertThat(allBassinellos.items()).hasSize(2);
    assertThat(allBassinellos.hasNext()).isFalse();
  }

}
