package com.arctura.balance_ledger.interfaces.rest.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

/**
 * Integration tests covering ledger cancellation behavior, soft-deleted
 * accounts, and balance protection rules.
 *
 * <p>The tests use real HTTP calls against the running Spring Boot application
 * to verify controller routing, persistence, transactional balance updates, and
 * the public JSON API contract together.</p>
 */
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
class LedgerCancellationIntegrationTests {
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
   * Verifies that cancelling an income creates a cancellation transaction and
   * subtracts the credited amount.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void cancellingIncomeCreatesCancelTransactionAndSubtractsAmount() throws Exception {
    UUID accountId = createAccount(new BigDecimal("100.00"));
    TransactionSnapshot income = createTransaction(accountId, null, "INCOME", new BigDecimal("50.00"));

    HttpResponse<String> cancelResponse = sendPostWithoutBody("/transactions/" + income.id() + "/cancel");

    assertEquals(201, cancelResponse.statusCode());
    assertEquals("CANCEL", extractString(cancelResponse.body(), "type"));
    assertBalance(accountId, new BigDecimal("100.00"));
    assertCancellationWasRecorded(income);
  }

  /**
   * Verifies that cancelling an expense creates a cancellation transaction and
   * restores the debited amount.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void cancellingExpenseCreatesCancelTransactionAndAddsAmountBack() throws Exception {
    UUID accountId = createAccount(new BigDecimal("100.00"));
    TransactionSnapshot expense = createTransaction(accountId, null, "EXPENSE", new BigDecimal("25.00"));

    HttpResponse<String> cancelResponse = sendPostWithoutBody("/transactions/" + expense.id() + "/cancel");

    assertEquals(201, cancelResponse.statusCode());
    assertEquals("CANCEL", extractString(cancelResponse.body(), "type"));
    assertBalance(accountId, new BigDecimal("100.00"));
    assertCancellationWasRecorded(expense);
  }

  /**
   * Verifies that cancelling a transfer moves funds back to the origin account
   * and keeps destination metadata on the cancellation record.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void cancellingTransferCreatesCancelTransactionAndMovesAmountBackToOrigin() throws Exception {
    UUID originAccountId = createAccount(new BigDecimal("100.00"));
    UUID destinationAccountId = createAccount(new BigDecimal("20.00"));
    TransactionSnapshot transfer = createTransaction(
      originAccountId,
      destinationAccountId,
      "TRANSFER",
      new BigDecimal("30.00")
    );

    HttpResponse<String> cancelResponse = sendPostWithoutBody("/transactions/" + transfer.id() + "/cancel");

    assertEquals(201, cancelResponse.statusCode());
    assertEquals("CANCEL", extractString(cancelResponse.body(), "type"));
    assertBalance(originAccountId, new BigDecimal("100.00"));
    assertBalance(destinationAccountId, new BigDecimal("20.00"));
    String cancellation = assertCancellationWasRecorded(transfer);
    assertEquals(destinationAccountId.toString(), extractString(cancellation, "destinationAccountId"));
  }

  /**
   * Verifies that a transaction cannot be cancelled more than once.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void cancellingTransactionTwiceReturnsConflict() throws Exception {
    UUID accountId = createAccount(new BigDecimal("100.00"));
    TransactionSnapshot income = createTransaction(accountId, null, "INCOME", new BigDecimal("10.00"));

    assertEquals(201, sendPostWithoutBody("/transactions/" + income.id() + "/cancel").statusCode());

    HttpResponse<String> secondCancelResponse = sendPostWithoutBody("/transactions/" + income.id() + "/cancel");

    assertEquals(409, secondCancelResponse.statusCode());
    assertTrue(secondCancelResponse.body().contains("\"errorCode\":\"TRANSACTION_ALREADY_CANCELLED\""));
  }

  /**
   * Verifies that account deletion hides the account while preserving historical
   * transactions.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void deletingAccountSoftDeletesAccountAndLeavesTransactionsUntouched() throws Exception {
    UUID accountId = createAccount(new BigDecimal("100.00"));
    TransactionSnapshot income = createTransaction(accountId, null, "INCOME", new BigDecimal("50.00"));

    HttpResponse<String> deleteResponse = sendDelete("/accounts/" + accountId);

    assertEquals(204, deleteResponse.statusCode());
    assertEquals(404, sendGet("/accounts/" + accountId).statusCode());
    String transactions = sendGet("/transactions").body();
    assertNotNull(findObjectWithFieldValue(transactions, "id", income.id().toString()));
    assertNull(findObjectWithFieldValue(transactions, "cancelledTransactionId", income.id().toString()));
  }

  /**
   * Verifies that transactions cannot be deleted through the API and that the
   * original balance remains intact.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void deletingTransactionEndpointIsNotExposed() throws Exception {
    UUID accountId = createAccount(new BigDecimal("100.00"));
    TransactionSnapshot income = createTransaction(accountId, null, "INCOME", new BigDecimal("10.00"));

    HttpResponse<String> deleteResponse = sendDelete("/transactions/" + income.id());

    assertEquals(405, deleteResponse.statusCode());
    assertBalance(accountId, new BigDecimal("110.00"));
  }

  /**
   * Verifies that standard expenses cannot create a negative balance.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void expenseThatWouldCreateNegativeBalanceReturnsInsufficientFunds() throws Exception {
    UUID accountId = createAccount(new BigDecimal("10.00"));

    HttpResponse<String> response = createTransactionResponse(
      accountId,
      null,
      "EXPENSE",
      new BigDecimal("15.00")
    );

    assertEquals(409, response.statusCode());
    assertTrue(response.body().contains("\"errorCode\":\"INSUFFICIENT_FUNDS\""));
    assertBalance(accountId, new BigDecimal("10.00"));
  }

  /**
   * Verifies that transfers cannot overdraw the origin account.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void transferThatWouldCreateNegativeBalanceReturnsInsufficientFunds() throws Exception {
    UUID originAccountId = createAccount(new BigDecimal("10.00"));
    UUID destinationAccountId = createAccount(new BigDecimal("0.00"));

    HttpResponse<String> response = createTransactionResponse(
      originAccountId,
      destinationAccountId,
      "TRANSFER",
      new BigDecimal("15.00")
    );

    assertEquals(409, response.statusCode());
    assertTrue(response.body().contains("\"errorCode\":\"INSUFFICIENT_FUNDS\""));
    assertBalance(originAccountId, new BigDecimal("10.00"));
    assertBalance(destinationAccountId, new BigDecimal("0.00"));
  }

  /**
   * Verifies that cancelling an income is blocked when the reversal would
   * overdraw the account.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void cancellingIncomeThatWouldCreateNegativeBalanceReturnsInsufficientFunds() throws Exception {
    UUID accountId = createAccount(new BigDecimal("0.00"));
    TransactionSnapshot income = createTransaction(accountId, null, "INCOME", new BigDecimal("50.00"));
    createTransaction(accountId, null, "EXPENSE", new BigDecimal("50.00"));

    HttpResponse<String> response = sendPostWithoutBody("/transactions/" + income.id() + "/cancel");

    assertEquals(409, response.statusCode());
    assertTrue(response.body().contains("\"errorCode\":\"INSUFFICIENT_FUNDS\""));
    assertBalance(accountId, new BigDecimal("0.00"));
    assertNull(findObjectWithFieldValue(sendGet("/transactions").body(), "cancelledTransactionId", income.id().toString()));
  }

  /**
   * Verifies that debt collection transactions may create a negative balance.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void debtCollectionCanCreateNegativeBalance() throws Exception {
    UUID accountId = createAccount(new BigDecimal("10.00"));

    TransactionSnapshot debtCollection = createTransaction(
      accountId,
      null,
      "DEBT_COLLECTION",
      new BigDecimal("25.00")
    );

    assertEquals("DEBT_COLLECTION", debtCollection.type());
    assertBalance(accountId, new BigDecimal("-15.00"));
  }

  /**
   * Verifies that cancelling a debt collection restores the account balance.
   *
   * @throws Exception when an HTTP request fails
   */
  @Test
  void cancellingDebtCollectionRestoresBalance() throws Exception {
    UUID accountId = createAccount(new BigDecimal("10.00"));
    TransactionSnapshot debtCollection = createTransaction(
      accountId,
      null,
      "DEBT_COLLECTION",
      new BigDecimal("25.00")
    );

    HttpResponse<String> cancelResponse = sendPostWithoutBody("/transactions/" + debtCollection.id() + "/cancel");

    assertEquals(201, cancelResponse.statusCode());
    assertEquals("CANCEL", extractString(cancelResponse.body(), "type"));
    assertBalance(accountId, new BigDecimal("10.00"));
    assertCancellationWasRecorded(debtCollection);
  }

  /**
   * Creates an account through the public API for test setup.
   *
   * @param balanceAmount initial balance amount
   * @return created account identifier
   * @throws Exception when the HTTP request fails
   */
  private UUID createAccount(BigDecimal balanceAmount) throws Exception {
    String body = """
      {
        "name": "Marco",
        "paternalSurname": "Test",
        "maternalSurname": "Account",
        "balanceAmount": %s,
        "balanceCurrency": "MXN"
      }
      """.formatted(balanceAmount);

    HttpResponse<String> response = sendPost("/accounts", body);

    assertEquals(200, response.statusCode());
    return UUID.fromString(extractString(response.body(), "id"));
  }

  /**
   * Creates a transaction through the public API and returns a compact parsed
   * snapshot for assertions.
   *
   * @param accountId source account identifier
   * @param destinationAccountId destination account identifier for transfers
   * @param type transaction type name
   * @param amount transaction amount
   * @return parsed transaction snapshot
   * @throws Exception when the HTTP request fails
   */
  private TransactionSnapshot createTransaction(
    UUID accountId,
    UUID destinationAccountId,
    String type,
    BigDecimal amount
  ) throws Exception {
    HttpResponse<String> response = createTransactionResponse(accountId, destinationAccountId, type, amount);

    assertEquals(200, response.statusCode());
    return new TransactionSnapshot(
      UUID.fromString(extractString(response.body(), "id")),
      UUID.fromString(extractString(response.body(), "accountId")),
      extractNullableUuid(response.body(), "destinationAccountId"),
      extractString(response.body(), "type"),
      extractDecimal(response.body(), "amount"),
      extractString(response.body(), "currency")
    );
  }

  /**
   * Sends a transaction creation request and returns the raw HTTP response.
   *
   * @param accountId source account identifier
   * @param destinationAccountId destination account identifier for transfers
   * @param type transaction type name
   * @param amount transaction amount
   * @return raw HTTP response
   * @throws Exception when the HTTP request fails
   */
  private HttpResponse<String> createTransactionResponse(
    UUID accountId,
    UUID destinationAccountId,
    String type,
    BigDecimal amount
  ) throws Exception {
    String destinationAccountJson = destinationAccountId == null
      ? ""
      : "\"destinationAccountId\": \"%s\",".formatted(destinationAccountId);
    String body = """
      {
        "accountId": "%s",
        %s
        "type": "%s",
        "amount": %s,
        "currency": "MXN",
        "description": "Test transaction"
      }
      """.formatted(accountId, destinationAccountJson, type, amount);

    return sendPost("/transactions", body);
  }

  /**
   * Asserts that a cancellation transaction exists and mirrors the original
   * transaction values.
   *
   * @param transaction original transaction snapshot
   * @return JSON object for the cancellation transaction
   * @throws Exception when the HTTP request fails
   */
  private String assertCancellationWasRecorded(TransactionSnapshot transaction) throws Exception {
    String transactions = sendGet("/transactions").body();
    String cancellation = findObjectWithFieldValue(transactions, "cancelledTransactionId", transaction.id().toString());

    assertNotNull(cancellation);
    assertEquals("CANCEL", extractString(cancellation, "type"));
    assertEquals(transaction.accountId().toString(), extractString(cancellation, "accountId"));
    assertEquals(0, transaction.amount().compareTo(extractDecimal(cancellation, "amount")));
    assertEquals(transaction.currency(), extractString(cancellation, "currency"));

    return cancellation;
  }

  /**
   * Asserts that an account has the expected balance amount.
   *
   * @param accountId account identifier
   * @param expectedBalance expected balance amount
   * @throws Exception when the HTTP request fails
   */
  private void assertBalance(UUID accountId, BigDecimal expectedBalance) throws Exception {
    String account = sendGet("/accounts/" + accountId).body();

    assertNotNull(account);
    assertEquals(0, expectedBalance.compareTo(extractDecimal(account, "balanceAmount")));
  }

  /**
   * Finds a flat JSON object containing an exact string field value.
   *
   * @param json JSON array or object text to search
   * @param field field name to match
   * @param value expected string value
   * @return matching JSON object text, or null when none is found
   */
  private String findObjectWithFieldValue(String json, String field, String value) {
    Pattern pattern = Pattern.compile("\\{[^{}]*\"" + field + "\":\"" + Pattern.quote(value) + "\"[^{}]*\\}");
    Matcher matcher = pattern.matcher(json);

    return matcher.find() ? matcher.group() : null;
  }

  /**
   * Extracts a string or null field value from a flat JSON object.
   *
   * @param json JSON text to inspect
   * @param field field name to extract
   * @return extracted string value, or null when the JSON field is null
   */
  private String extractString(String json, String field) {
    Pattern pattern = Pattern.compile("\"" + field + "\":(?:\"([^\"]*)\"|null)");
    Matcher matcher = pattern.matcher(json);

    if (!matcher.find()) {
      throw new AssertionError("Expected field " + field + " in " + json);
    }

    return matcher.group(1);
  }

  /**
   * Extracts an optional UUID field value from a flat JSON object.
   *
   * @param json JSON text to inspect
   * @param field field name to extract
   * @return parsed UUID, or null when the JSON field is null
   */
  private UUID extractNullableUuid(String json, String field) {
    String value = extractString(json, field);

    return value == null ? null : UUID.fromString(value);
  }

  /**
   * Extracts a decimal field value from a flat JSON object.
   *
   * @param json JSON text to inspect
   * @param field field name to extract
   * @return parsed decimal value
   */
  private BigDecimal extractDecimal(String json, String field) {
    Pattern pattern = Pattern.compile("\"" + field + "\":(-?[0-9.]+)");
    Matcher matcher = pattern.matcher(json);

    if (!matcher.find()) {
      throw new AssertionError("Expected field " + field + " in " + json);
    }

    return new BigDecimal(matcher.group(1));
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
   * Sends a POST request without a body to the local test server.
   *
   * @param path request path beginning with a slash
   * @return HTTP response body as a string
   * @throws Exception when request creation or execution fails
   */
  private HttpResponse<String> sendPostWithoutBody(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(uri(path))
      .POST(HttpRequest.BodyPublishers.noBody())
      .build();

    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  /**
   * Sends a DELETE request to the local test server.
   *
   * @param path request path beginning with a slash
   * @return HTTP response body as a string
   * @throws Exception when request creation or execution fails
   */
  private HttpResponse<String> sendDelete(String path) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(uri(path)).DELETE().build();
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

  /**
   * Compact projection used by tests to assert transaction identity and ledger
   * values across API calls.
   *
   * @param id transaction identifier
   * @param accountId source account identifier
   * @param destinationAccountId destination account identifier, or null
   * @param type transaction type name
   * @param amount transaction amount
   * @param currency transaction currency name
   */
  private record TransactionSnapshot(
    UUID id,
    UUID accountId,
    UUID destinationAccountId,
    String type,
    BigDecimal amount,
    String currency
  ) {}
}
