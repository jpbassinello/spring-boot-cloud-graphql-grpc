package br.com.jpbassinello.sbcgg.graphql.gateway.config;

import org.springframework.boot.graphql.autoconfigure.GraphQlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, GraphQlProperties graphQlProperties) throws Exception {

    if (graphQlProperties.getSchema().getPrinter().isEnabled()) {
      http
          .authorizeHttpRequests(
              authorize -> authorize.requestMatchers("/graphql/schema").permitAll()
          );
    }

    if (graphQlProperties.getGraphiql().isEnabled()) {
      http
          .authorizeHttpRequests(
              authorize -> authorize.requestMatchers("/graphiql").permitAll()
          );
    }

    http
        .csrf(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(
            customizer -> customizer
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(
            authorize -> authorize.requestMatchers("/actuator/**").permitAll()
        )
        .authorizeHttpRequests(
            authorize -> authorize
                .anyRequest()
                .hasRole("USER")
        ).oauth2ResourceServer(
            customizer -> customizer.jwt(
                jwtCustomizer -> jwtCustomizer.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())
            )
        );

    return http.build();
  }

  static class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
      var realmAccess = (Map<String, List<String>>) jwt.getClaims().get("realm_access");
      var grantedAuthorities = realmAccess.get("roles").stream()
          .map(roleName -> "ROLE_" + roleName)
          .map(SimpleGrantedAuthority::new)
          .map(simpleGrantedAuthority -> (GrantedAuthority) simpleGrantedAuthority)
          .toList();

      return new JwtAuthenticationToken(jwt, grantedAuthorities);
    }
  }
}
