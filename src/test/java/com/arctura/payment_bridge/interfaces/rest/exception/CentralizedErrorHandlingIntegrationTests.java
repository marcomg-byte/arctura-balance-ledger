package com.arctura.payment_bridge.interfaces.rest.exception;

import java.util.UUID;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
class CentralizedErrorHandlingIntegrationTests {
  private HttpClient client;

  @LocalServerPort
  private int port;

  @BeforeEach
  void setUp() {
    this.client = HttpClient.newHttpClient();
  }

  @Test
  void returnsStructuredErrorForMissingAccount() throws Exception {
    HttpResponse<String> response = sendGet("/accounts/" + UUID.randomUUID());

    assertEquals(404, response.statusCode());
    assertFalse(response.headers().firstValue("X-Correlation-Id").orElse("").isBlank());
    assertTrue(response.body().contains("\"status\":404"));
    assertTrue(response.body().contains("\"errorCode\":\"ACCOUNT_NOT_FOUND\""));
  }

  @Test
  void returnsStructuredErrorForInvalidTransactionId() throws Exception {
    HttpResponse<String> response = sendGet("/transactions/not-a-uuid");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("\"status\":400"));
    assertTrue(response.body().contains("\"errorCode\":\"INVALID_TRANSACTION_ID\""));
  }

  @Test
  void returnsStructuredErrorForMissingRequestProperties() throws Exception {
    HttpResponse<String> response = sendPost("/accounts", "{\"name\":\"Marco\"}");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("\"status\":400"));
    assertTrue(response.body().contains("\"errorCode\":\"MISSING_REQUEST_BODY_PROPS\""));
  }

  @Test
  void returnsStructuredErrorForMalformedRequestBody() throws Exception {
    HttpResponse<String> response = sendPost("/transactions", "{\"type\":\"NOT_A_TYPE\"}");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("\"status\":400"));
    assertTrue(response.body().contains("\"errorCode\":\"MALFORMED_REQUEST_BODY\""));
  }

  @Test
  void returnsStructuredErrorWhenTransactionAccountDoesNotExist() throws Exception {
    String requestBody = """
      {
        "accountId": "%s",
        "type": "INCOME",
        "amount": 100.00,
        "currency": "MXN",
        "description": "Initial deposit"
      }
      """.formatted(UUID.randomUUID());

    HttpResponse<String> response = sendPost("/transactions", requestBody);

    assertEquals(404, response.statusCode());
    assertTrue(response.body().contains("\"status\":404"));
    assertTrue(response.body().contains("\"errorCode\":\"ACCOUNT_NOT_FOUND\""));
  }

  private HttpResponse<String> sendGet(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> sendPost(String path, String body) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(uri(path))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private URI uri(String path) {
    return URI.create("http://localhost:" + this.port + path);
  }
}
