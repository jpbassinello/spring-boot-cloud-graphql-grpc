package br.com.jpbassinello.sbcgg.graphql.gateway.domain.types;

import java.util.List;

public record UserPage(
    List<User> users,
    boolean hasNext
) {}
