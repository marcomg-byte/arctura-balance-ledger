package com.arctura.payment_bridge.interfaces.rest.transactions.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

@RestController
@RequestMapping("/transactions")
public class TransactionController {
  private final TransactionRepository transactionRepository;
  private final AccountRepository accountRepository;

  public TransactionController(
    TransactionRepository transactionRepository,
    AccountRepository accountRepository
  ) {
    this.transactionRepository = transactionRepository;
    this.accountRepository = accountRepository;
  }

  @PostMapping
  public TransactionResponse create(@RequestBody(required = false) CreateTransactionRequest request) {
    validateCreateRequest(request);
    ensureAccountExists(request.accountId());

    Transaction transaction = new Transaction(
      UUID.randomUUID(),
      request.accountId(),
      request.type(),
      new Money(request.amount(), request.currency()),
      request.description()
    );

    return TransactionResponse.from(this.transactionRepository.save(transaction));
  }

  @GetMapping("/{id}")
  public TransactionResponse findById(@PathVariable String id) {
    UUID transactionId = parseTransactionId(id);

    return this.transactionRepository.findById(transactionId)
      .map(TransactionResponse::from)
      .orElseThrow(TransactionNotFoundException::new);
  }
  
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

  @PatchMapping("/{id}")
  public TransactionResponse update(
    @PathVariable String id,
    @RequestBody(required = false) UpdateTransactionRequest request
  ) {
    UUID transactionId = parseTransactionId(id);
    validateUpdateRequest(request);

    Transaction transaction = this.transactionRepository.findById(transactionId)
      .orElseThrow(TransactionNotFoundException::new);

    transaction.update(
      request.type(),
      new Money(request.amount(), request.currency()),
      request.description()
    );

    return TransactionResponse.from(this.transactionRepository.save(transaction));
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    UUID transactionId = parseTransactionId(id);

    if (!this.transactionRepository.existsById(transactionId)) {
      throw new TransactionNotFoundException();
    }

    this.transactionRepository.deleteById(transactionId);
    return ResponseEntity.noContent().build();
  }

  private UUID parseAccountId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      throw new InvalidAccountIdException(id);
    }
  }

  private UUID parseTransactionId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      throw new InvalidTransactionIdException(id);
    }
  }

  private void ensureAccountExists(UUID accountId) {
    if (!this.accountRepository.existsById(accountId)) {
      throw new AccountNotFoundException();
    }
  }

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

  private void validateUpdateRequest(UpdateTransactionRequest request) {
    if (request == null) {
      throw new MissingRequestBodyException();
    }

    List<String> missingProps = new ArrayList<>();

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

  private void throwIfMissingProps(List<String> missingProps) {
    if (!missingProps.isEmpty()) {
      throw new MissingRequestBodyPropsException(missingProps.toArray(String[]::new));
    }
  }

}
