package com.arctura.payment_bridge.interfaces.rest.transactions.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.payment_bridge.domain.shared.Currency;
import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

/**
 * API response projection for ledger transaction details and cancellation
 * relationships.
 *
 * @param id transaction identifier
 * @param accountId source account identifier
 * @param destinationAccountId destination account identifier for transfers
 * @param cancelledTransactionId original transaction id for cancellation records
 * @param type ledger operation type
 * @param amount transaction amount
 * @param currency transaction currency
 * @param description human-readable transaction description
 * @param createdAt creation timestamp
 */
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
  /**
   * Creates an API response projection from a transaction aggregate.
   *
   * @param transaction domain transaction to project
   * @return transaction response ready for JSON serialization
   */
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
