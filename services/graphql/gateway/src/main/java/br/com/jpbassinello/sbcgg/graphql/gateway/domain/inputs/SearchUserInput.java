package br.com.jpbassinello.sbcgg.graphql.gateway.domain.inputs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SearchUserInput(
    @Size(min = 3)
    String terms,
    @Min(0)
    int page,
    @Min(1)
    @Max(200)
    int pageSize
) {}
