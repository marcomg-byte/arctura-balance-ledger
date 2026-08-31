package com.arctura.payment_bridge.interfaces.rest.transactions.controllers;

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

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionRepository;
import com.arctura.payment_bridge.interfaces.rest.transactions.requests.CreateTransactionRequest;
import com.arctura.payment_bridge.interfaces.rest.transactions.requests.UpdateTransactionRequest;
import com.arctura.payment_bridge.interfaces.rest.transactions.responses.TransactionResponse;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
  private final TransactionRepository transactionRepository;

  public TransactionController(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  @PostMapping
  public TransactionResponse create(@RequestBody CreateTransactionRequest request) {
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
  public ResponseEntity<TransactionResponse> findById(@PathVariable UUID id) {
      return this.transactionRepository.findById(id)
        .map(TransactionResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
  
  @GetMapping
  public List<TransactionResponse> findAll(@RequestParam(required = false) UUID accountId) {
    List<Transaction> transactions = accountId == null
      ? this.transactionRepository.findAll()
      : this.transactionRepository.findByAccountId(accountId);

    return transactions.stream()
      .map(TransactionResponse::from)
      .toList();
  }

  @PatchMapping("/{id}")
  public ResponseEntity<TransactionResponse> update(
    @PathVariable UUID id,
    @RequestBody UpdateTransactionRequest request
  ) {
    return this.transactionRepository.findById(id)
      .map(transaction -> {
        transaction.update(
          request.type(),
          new Money(request.amount(), request.currency()),
          request.description()
        );

        return TransactionResponse.from(this.transactionRepository.save(transaction));
      })
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
    if (!this.transactionRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    this.transactionRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

}
