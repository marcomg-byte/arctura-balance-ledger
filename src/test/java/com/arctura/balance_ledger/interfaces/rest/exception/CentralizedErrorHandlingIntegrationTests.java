package com.arctura.balance_ledger.interfaces.rest.exception;

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

/**
 * Integration tests for the centralized REST error contract and correlation id
 * propagation.
 *
 * <p>The tests exercise real HTTP requests against a random local port so the
 * controller, filter, exception handler, and JSON serialization layers are
 * verified together.</p>
 */
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
class CentralizedErrorHandlingIntegrationTests {
  private HttpClient client;

  @LocalServerPort
  private int port;

  /**
   * Creates a fresh HTTP client before each test.
   */
  @BeforeEach
  void setUp() {
    this.client = HttpClient.newHttpClient();
  }

  /**
   * Verifies that missing domain resources are translated into structured 404
   * responses with correlation ids.
   *
   * @throws Exception when the HTTP request fails
   */
  @Test
  void returnsStructuredErrorForMissingAccount() throws Exception {
    HttpResponse<String> response = sendGet("/accounts/" + UUID.randomUUID());

    assertEquals(404, response.statusCode());
    assertFalse(response.headers().firstValue("X-Correlation-Id").orElse("").isBlank());
    assertTrue(response.body().contains("\"status\":404"));
    assertTrue(response.body().contains("\"errorCode\":\"ACCOUNT_NOT_FOUND\""));
  }

  /**
   * Verifies that invalid transaction UUIDs are reported as request errors.
   *
   * @throws Exception when the HTTP request fails
   */
  @Test
  void returnsStructuredErrorForInvalidTransactionId() throws Exception {
    HttpResponse<String> response = sendGet("/transactions/not-a-uuid");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("\"status\":400"));
    assertTrue(response.body().contains("\"errorCode\":\"INVALID_TRANSACTION_ID\""));
  }

  /**
   * Verifies that request body validation returns a stable missing-properties
   * error code.
   *
   * @throws Exception when the HTTP request fails
   */
  @Test
  void returnsStructuredErrorForMissingRequestProperties() throws Exception {
    HttpResponse<String> response = sendPost("/accounts", "{\"name\":\"Marco\"}");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("\"status\":400"));
    assertTrue(response.body().contains("\"errorCode\":\"MISSING_REQUEST_BODY_PROPS\""));
  }

  /**
   * Verifies that malformed or unparseable JSON payload values use the
   * malformed-body error code.
   *
   * @throws Exception when the HTTP request fails
   */
  @Test
  void returnsStructuredErrorForMalformedRequestBody() throws Exception {
    HttpResponse<String> response = sendPost("/transactions", "{\"type\":\"NOT_A_TYPE\"}");

    assertEquals(400, response.statusCode());
    assertTrue(response.body().contains("\"status\":400"));
    assertTrue(response.body().contains("\"errorCode\":\"MALFORMED_REQUEST_BODY\""));
  }

  /**
   * Verifies that application-layer account lookups are translated into
   * structured not-found responses.
   *
   * @throws Exception when the HTTP request fails
   */
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

  /**
   * Sends a GET request to the local test server.
   *
   * @param path request path beginning with a slash
   * @return HTTP response body as a string
   * @throws Exception when request creation or execution fails
   */
  private HttpResponse<String> sendGet(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  /**
   * Sends a JSON POST request to the local test server.
   *
   * @param path request path beginning with a slash
   * @param body JSON request body
   * @return HTTP response body as a string
   * @throws Exception when request creation or execution fails
   */
  private HttpResponse<String> sendPost(String path, String body) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(uri(path))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  /**
   * Builds an absolute URI for the random-port test server.
   *
   * @param path request path beginning with a slash
   * @return absolute local test-server URI
   */
  private URI uri(String path) {
    return URI.create("http://localhost:" + this.port + path);
  }
}
