package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.jpa.domain.entities.SimplePage;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.LoadUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.application.port.out.PersistUserPort;
import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
class UserPersistenceAdapter implements PersistUserPort, LoadUserPort {

  private final UserRepository userRepository;

  @Override
  public User save(User user) {
    if (user.getRegisteredAt() == null) {
      user.setRegisteredAt(ZonedDateTime.now());
    }
    return userRepository.save(user);
  }

  @Override
  public Optional<User> loadUserById(UUID id) {
    return userRepository.findById(id);
  }

  @Override
  public Optional<User> loadUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public Optional<User> loadUserByMobilePhoneNumber(String mobilePhoneNumber) {
    return userRepository.findByMobilePhoneNumber(mobilePhoneNumber);
  }

  @Override
  public SimplePage<User> search(String terms, int page, int pageSize) {
    if (pageSize > 200) {
      throw new IllegalArgumentException("Requested to fetch more users than allowed");
    }

    // using pageSize + 1 to check if provided search terms has more elements to show next
    var results = userRepository.search(terms, pageSize + 1, page * pageSize);
    var hasNext = results.size() > pageSize;
    return new SimplePage<>(
        results.subList(0, Math.min(results.size(), pageSize)), // only returning what is expected in the page
        hasNext
    );
  }
}
