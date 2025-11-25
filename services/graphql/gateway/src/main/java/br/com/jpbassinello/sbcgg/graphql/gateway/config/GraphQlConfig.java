package br.com.jpbassinello.sbcgg.graphql.gateway.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
class GraphQlConfig {

  @Bean
  public RuntimeWiringConfigurer runtimeWiringConfigurer() {
    return wiringBuilder -> wiringBuilder
        .scalar(ExtendedScalars.Date)
        .scalar(ExtendedScalars.DateTime)
        .scalar(ExtendedScalars.UUID)
        .scalar(ExtendedScalars.GraphQLBigDecimal);
  }

}
