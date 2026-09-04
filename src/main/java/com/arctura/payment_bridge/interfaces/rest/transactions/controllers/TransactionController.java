package com.arctura.payment_bridge.interfaces.rest.transactions.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arctura.payment_bridge.application.transactions.CancelTransactionService;
import com.arctura.payment_bridge.application.transactions.RecordTransactionCommand;
import com.arctura.payment_bridge.application.transactions.RecordTransactionService;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;
import com.arctura.payment_bridge.domain.exception.TransactionNotFoundException;
import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionRepository;
import com.arctura.payment_bridge.interfaces.rest.transactions.requests.CreateTransactionRequest;
import com.arctura.payment_bridge.interfaces.rest.transactions.requests.UpdateTransactionRequest;
import com.arctura.payment_bridge.interfaces.rest.transactions.responses.TransactionResponse;
import com.arctura.payment_bridge.interfaces.rest.exception.request.InvalidAccountIdException;
import com.arctura.payment_bridge.interfaces.rest.exception.request.InvalidTransactionIdException;
import com.arctura.payment_bridge.interfaces.rest.exception.request.MissingRequestBodyException;
import com.arctura.payment_bridge.interfaces.rest.exception.request.MissingRequestBodyPropsException;
import com.arctura.payment_bridge.interfaces.rest.exception.request.ReadOnlyRequestBodyPropsException;

/**
 * REST controller exposing ledger transaction operations.
 *
 * <p>The controller validates HTTP request shape, parses UUID path/query
 * parameters, and delegates balance-affecting workflows to application services
 * so transaction rules remain outside the web layer.</p>
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {
  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;
  private final RecordTransactionService recordTransactionService;
  private final CancelTransactionService cancelTransactionService;

  /**
   * Creates the controller with repositories and use-case services required by
   * transaction endpoints.
   *
   * @param transactionRepository repository port used for transaction lookup and
   *                              persistence
   * @param accountRepository repository port used to validate account filters
   * @param recordTransactionService use case service that records transactions
   * @param cancelTransactionService use case service that cancels transactions
   */
  public TransactionController(
    TransactionRepository transactionRepository,
    AccountRepository accountRepository,
    RecordTransactionService recordTransactionService,
    CancelTransactionService cancelTransactionService
  ) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
    this.recordTransactionService = recordTransactionService;
    this.cancelTransactionService = cancelTransactionService;
  }

  /**
   * Records a transaction from a JSON request body.
   *
   * @param request transaction creation payload, required by this endpoint
   * @return serialized transaction response for the saved ledger entry
   * @throws MissingRequestBodyException when the body is absent
   * @throws MissingRequestBodyPropsException when required fields are missing
   * @throws AccountNotFoundException when an involved account does not exist
   */
  @PostMapping
  public TransactionResponse create(@RequestBody(required = false) CreateTransactionRequest request) {
    validateCreateRequest(request);

    Transaction transaction = this.recordTransactionService.record(
      new RecordTransactionCommand(
        request.accountId(),
        request.destinationAccountId(),
        request.type(),
        new Money(request.amount(), request.currency()),
        request.description()
      )
    );

    return TransactionResponse.from(transaction);
  }

  /**
   * Finds a transaction by UUID path parameter.
   *
   * @param id raw transaction id path parameter
   * @return serialized transaction response
   * @throws InvalidTransactionIdException when the path parameter is not a UUID
   * @throws TransactionNotFoundException when no transaction exists for the id
   */
  @GetMapping("/{id}")
  public TransactionResponse findById(@PathVariable String id) {
    UUID transactionId = parseTransactionId(id);

    return this.transactionRepository.findById(transactionId)
      .map(TransactionResponse::from)
      .orElseThrow(TransactionNotFoundException::new);
  }
  
  /**
   * Lists transactions, optionally limited to entries where the account
   * participates as source or destination.
   *
   * @param accountId optional raw account id query parameter
   * @return serialized transaction responses
   * @throws InvalidAccountIdException when the account filter is not a UUID
   * @throws AccountNotFoundException when the account filter references a
   *                                  missing account
   */
  @GetMapping
  public List<TransactionResponse> findAll(@RequestParam(required = false) String accountId) {
    UUID parsedAccountId = accountId == null || accountId.isBlank()
      ? null
      : parseAccountId(accountId);

    if (parsedAccountId != null) {
      ensureAccountExists(parsedAccountId);
    }

    List<Transaction> transactions = parsedAccountId == null
      ? this.transactionRepository.findAll()
      : this.transactionRepository.findByAccountId(parsedAccountId);

    return transactions.stream()
      .map(TransactionResponse::from)
      .toList();
  }

  /**
   * Updates the mutable description of a transaction.
   *
   * @param id raw transaction id path parameter
   * @param request transaction update payload
   * @return serialized transaction response after persistence
   * @throws InvalidTransactionIdException when the path parameter is not a UUID
   * @throws MissingRequestBodyException when the body is absent
   * @throws ReadOnlyRequestBodyPropsException when immutable ledger fields are
   *                                          present in the payload
   * @throws MissingRequestBodyPropsException when the description field is
   *                                         missing
   * @throws TransactionNotFoundException when no transaction exists for the id
   */
  @PatchMapping("/{id}")
  public TransactionResponse update(
    @PathVariable String id,
    @RequestBody(required = false) UpdateTransactionRequest request
  ) {
    UUID transactionId = parseTransactionId(id);
    validateUpdateRequest(request);

    Transaction transaction = this.transactionRepository.findById(transactionId)
      .orElseThrow(TransactionNotFoundException::new);

    transaction.updateDescription(request.description());

    return TransactionResponse.from(this.transactionRepository.save(transaction));
  }
  
  /**
   * Cancels a transaction by creating and returning a cancellation ledger entry.
   *
   * @param id raw transaction id path parameter
   * @return HTTP 201 response containing the generated cancellation transaction
   * @throws InvalidTransactionIdException when the path parameter is not a UUID
   * @throws TransactionNotFoundException when no transaction exists for the id
   */
  @PostMapping("/{id}/cancel")
  public ResponseEntity<TransactionResponse> cancelById(@PathVariable String id) {
    UUID transactionId = parseTransactionId(id);

    Transaction cancellation = this.cancelTransactionService.cancel(transactionId);

    return ResponseEntity
      .created(URI.create("/transactions/" + cancellation.getId()))
      .body(TransactionResponse.from(cancellation));
  }

  /**
   * Parses an account id from a query-string value.
   *
   * @param id raw id value supplied by the client
   * @return parsed account UUID
   * @throws InvalidAccountIdException when the value is not a UUID
   */
  private UUID parseAccountId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      throw new InvalidAccountIdException(id);
    }
  }

  /**
   * Parses a transaction id from a URL value.
   *
   * @param id raw id value supplied by the client
   * @return parsed transaction UUID
   * @throws InvalidTransactionIdException when the value is not a UUID
   */
  private UUID parseTransactionId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      throw new InvalidTransactionIdException(id);
    }
  }

  /**
   * Ensures an account exists before applying a transaction filter.
   *
   * @param accountId parsed account identifier
   * @throws AccountNotFoundException when no active account exists for the id
   */
  private void ensureAccountExists(UUID accountId) {
    if (!this.accountRepository.existsById(accountId)) {
      throw new AccountNotFoundException();
    }
  }

  /**
   * Validates required properties for transaction creation.
   *
   * @param request request body to validate
   * @throws MissingRequestBodyException when the request body is absent
   * @throws MissingRequestBodyPropsException when required properties are absent
   */
  private void validateCreateRequest(CreateTransactionRequest request) {
    if (request == null) {
      throw new MissingRequestBodyException();
    }

    List<String> missingProps = new ArrayList<>();

    if (request.accountId() == null) {
      missingProps.add("accountId");
    }

    if (request.type() == null) {
      missingProps.add("type");
    }

    if (request.amount() == null) {
      missingProps.add("amount");
    }

    if (request.currency() == null) {
      missingProps.add("currency");
    }

    throwIfMissingProps(missingProps);
  }

  /**
   * Validates transaction update payloads and rejects attempts to mutate
   * immutable ledger values.
   *
   * @param request request body to validate
   * @throws MissingRequestBodyException when the request body is absent
   * @throws ReadOnlyRequestBodyPropsException when read-only properties are
   *                                          supplied
   * @throws MissingRequestBodyPropsException when the description property is
   *                                         absent
   */
  private void validateUpdateRequest(UpdateTransactionRequest request) {
    if (request == null) {
      throw new MissingRequestBodyException();
    }

    List<String> readOnlyProps = new ArrayList<>();

    if (request.type() != null) {
      readOnlyProps.add("type");
    }

    if (request.amount() != null) {
      readOnlyProps.add("amount");
    }

    if (request.currency() != null) {
      readOnlyProps.add("currency");
    }

    if (!readOnlyProps.isEmpty()) {
      throw new ReadOnlyRequestBodyPropsException(readOnlyProps.toArray(String[]::new));
    }

    List<String> missingProps = new ArrayList<>();

    if (request.description() == null) {
      missingProps.add("description");
    }

    throwIfMissingProps(missingProps);
  }

  /**
   * Raises a request validation exception when any required properties are
   * missing.
   *
   * @param missingProps missing request property names
   * @throws MissingRequestBodyPropsException when the list is not empty
   */
  private void throwIfMissingProps(List<String> missingProps) {
    if (!missingProps.isEmpty()) {
      throw new MissingRequestBodyPropsException(missingProps.toArray(String[]::new));
    }
  }

}
