package br.com.jpbassinello.sbcgg.jpa.domain.entities;

import java.util.List;

public record SimplePage<T>(
    List<T> items,
    boolean hasNext
) {
}
