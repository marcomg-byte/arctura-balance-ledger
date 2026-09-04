package com.arctura.payment_bridge.interfaces.rest.transactions.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.payment_bridge.domain.shared.Currency;
import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

public record TransactionResponse(
  UUID id,
  UUID accountId,
  UUID destinationAccountId,
  UUID cancelledTransactionId,
  TransactionType type,
  BigDecimal amount,
  Currency currency,
  String description,
  LocalDateTime createdAt
) {
  public static TransactionResponse from(Transaction transaction) {
    Money amount = transaction.getAmount();

    return new TransactionResponse(
      transaction.getId(),
      transaction.getAccountId(),
      transaction.getDestinationAccountId(),
      transaction.getCancelledTransactionId(),
      transaction.getType(),
      amount.getAmount(),
      amount.getCurrency(),
      transaction.getDescription(),
      transaction.getCreatedAt()
    );
  }
}
