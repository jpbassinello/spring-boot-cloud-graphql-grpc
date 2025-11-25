package br.com.jpbassinello.sbcgg.services.grpc.users.adapter.out.persistence;

import br.com.jpbassinello.sbcgg.services.grpc.users.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  Optional<User> findByMobilePhoneNumber(String mobilePhoneNumber);

  @Query(value =
      "SELECT * FROM users u " +
          "WHERE u.terms @@ PLAINTO_TSQUERY(LOWER(UNACCENT(:terms))) " +
          "ORDER BY UNACCENT(u.first_name), UNACCENT(u.last_name) " +
          "LIMIT :limit " +
          "OFFSET :offset", nativeQuery = true)
  List<User> search(@Param("terms") String terms, @Param("limit") int limit, @Param("offset") int offset);
}
