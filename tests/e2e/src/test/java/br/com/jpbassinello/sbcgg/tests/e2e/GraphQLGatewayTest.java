package br.com.jpbassinello.sbcgg.tests.e2e;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes = {GraphQLGatewayTest.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableConfigurationProperties(GraphQLGatewayTest.Config.class)
@AutoConfigureWebClient
class GraphQLGatewayTest {

  private static final String KEYCLOAK_TOKEN_ENDPOINT = "/realms/sbcgg/protocol/openid-connect/token";

  @Autowired
  private RestTemplateBuilder restTemplateBuilder;
  @Autowired
  private GraphQLGatewayTest.Config config;

  private GraphQlTester graphQlTester;
  private TestRestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    restTemplate = new TestRestTemplate(
        restTemplateBuilder
            .rootUri(config.keycloakUrl)
    );

    var adminAccessToken = getAccessToken("admin@sbcgg.com", "admin");

    graphQlTester = HttpGraphQlTester.builder(
            WebTestClient.bindToServer()
                .baseUrl(config.graphQLGatewayUrl)
        ).header("authorization", "Bearer " + adminAccessToken)
        .build();
  }


  private void registerAndValidateRandomUser() throws IOException {
    var registerUserMutation = IOUtils.toString(
        GraphQLGatewayTest.class.getResourceAsStream("/registerUser.mutation.graphqls"),
        StandardCharsets.UTF_8
    );

    var random = UUID.randomUUID();

    var phoneSufix = random.toString().replaceAll("\\D", "").substring(0, 7);

    var userId = graphQlTester
        .document(registerUserMutation)
        .variables(
            Map.of(
                "email", random + "@sbcgg.com.br",
                "firstName", "User",
                "lastName", random,
                "password", random,
                "timeZoneId", "America/Sao_Paulo",
                "mobilePhoneNumber", "+551999" + phoneSufix
            )
        )
        .execute()

        .path("registerUser.emailVerified").entity(Boolean.class).isEqualTo(false)
        .path("registerUser.mobilePhoneNumberVerified").entity(Boolean.class).isEqualTo(false)
        .path("registerUser.roles").entity(List.class).isEqualTo(List.of("USER"))
        .path("registerUser.id").entity(UUID.class).get();

    System.out.println(userId);
  }

  @Test
  void createAndRetrieveUser() throws IOException {
    registerAndValidateRandomUser();
  }

  private String getAccessToken(String username, String password) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    var parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("grant_type", "password"); // Or "password", "authorization_code", etc.
    parameters.add("client_id", "sbcgg");
    parameters.add("client_secret", "GBQWoDGXmVRdoQzxBttBbx0BFaJL3Xoy");
    parameters.add("scope", "openid profile email");
    parameters.add("username", username);
    parameters.add("password", password);

    var entity = new HttpEntity<MultiValueMap<String, String>>(parameters, headers);
    return restTemplate.postForObject(
        KEYCLOAK_TOKEN_ENDPOINT,
        entity,
        TokenResponse.class
    ).accessToken();
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  private record TokenResponse(String accessToken) {}

  @ConfigurationProperties(prefix = "test.e2e")
  @Data
  public static class Config {
    private String graphQLGatewayUrl;
    private String keycloakUrl;
  }

}
