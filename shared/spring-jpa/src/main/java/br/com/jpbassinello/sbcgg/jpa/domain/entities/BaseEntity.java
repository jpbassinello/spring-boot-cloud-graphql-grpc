package br.com.jpbassinello.sbcgg.jpa.domain.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity implements Serializable {

  @Id
  @Column(name = "id")
  @NotNull
  private UUID id;

  @PrePersist
  void prePersist() {
    if (id == null) {
      id = UuidCreator.getShortPrefixComb();
    }
  }
}
