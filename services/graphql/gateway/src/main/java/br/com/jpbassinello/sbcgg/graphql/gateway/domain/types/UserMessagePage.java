package br.com.jpbassinello.sbcgg.graphql.gateway.domain.types;

import java.util.List;

public record UserMessagePage(
    List<Message> messages,
    boolean hasNext
) {}
