package com.arctura.payment_bridge.domain.transaction;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.shared.Money;

public class Transaction {
  private final UUID id;
  private final UUID accountId;
  private final UUID destinationAccountId;
  private final UUID cancelledTransactionId;
  private final TransactionType type;
  private final Money amount;
  private String description;
  private final LocalDateTime createdAt;

  public Transaction(
    UUID id,
    UUID accountId,
    UUID destinationAccountId,
    TransactionType type,
    Money amount,
    String description
  ) {
    this(id, accountId, destinationAccountId, type, amount, description, LocalDateTime.now());
  }

  public Transaction(
    UUID id,
    UUID accountId,
    UUID destinationAccountId,
    UUID cancelledTransactionId,
    TransactionType type,
    Money amount,
    String description
  ) {
    this(id, accountId, destinationAccountId, cancelledTransactionId, type, amount, description, LocalDateTime.now());
  }

  public Transaction(
    UUID id,
    UUID accountId,
    UUID destinationAccountId,
    TransactionType type,
    Money amount,
    String description,
    LocalDateTime createdAt
  ) {
    this(id, accountId, destinationAccountId, null, type, amount, description, createdAt);
  }

  public Transaction(
    UUID id,
    UUID accountId,
    UUID destinationAccountId,
    UUID cancelledTransactionId,
    TransactionType type,
    Money amount,
    String description,
    LocalDateTime createdAt
  ) {
    if (id == null) {
      throw new DomainValidationException("Transaction id is required");
    }

    if (accountId == null) {
      throw new DomainValidationException("Account id is required");
    }

    if (type == null) {
      throw new DomainValidationException("Transaction type is required");
    }

    if (amount == null) {
      throw new DomainValidationException("Transaction amount is required");
    }

    if (createdAt == null) {
      throw new DomainValidationException("Transaction creation date is required");
    }

    if (type == TransactionType.TRANSFER) {
      if (destinationAccountId == null) {
        throw new DomainValidationException("Destination account id is required for transfers");
      }

      if (accountId.equals(destinationAccountId)) {
        throw new DomainValidationException("Transfer source and destination accounts must be different");
      }
    }

    if (type == TransactionType.CANCEL) {
      if (cancelledTransactionId == null) {
        throw new DomainValidationException("Cancelled transaction id is required for cancel transactions");
      }

      if (id.equals(cancelledTransactionId)) {
        throw new DomainValidationException("Cancel transaction cannot reference itself");
      }

      if (destinationAccountId != null && accountId.equals(destinationAccountId)) {
        throw new DomainValidationException("Cancel source and destination accounts must be different");
      }
    }

    if (type != TransactionType.CANCEL && cancelledTransactionId != null) {
      throw new DomainValidationException("Cancelled transaction id is only allowed for cancel transactions");
    }

    if (type != TransactionType.TRANSFER && type != TransactionType.CANCEL && destinationAccountId != null) {
      throw new DomainValidationException("Destination account id is only allowed for transfers and transfer cancellations");
    }

    this.id = id;
    this.accountId = accountId;
    this.destinationAccountId = destinationAccountId;
    this.cancelledTransactionId = cancelledTransactionId;
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.createdAt = createdAt;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public UUID getDestinationAccountId() {
    return this.destinationAccountId;
  }

  public UUID getCancelledTransactionId() {
    return this.cancelledTransactionId;
  }

  public TransactionType getType() {
    return type;
  }

  public Money getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "Transaction{" +
        "id='" + id + '\'' +
        ", accountId='" + accountId + '\'' +
        ", destinationAccountId='" + destinationAccountId + '\'' +
        ", cancelledTransactionId='" + cancelledTransactionId + '\'' +
        ", type=" + type +
        ", amount=" + amount +
        ", description='" + description + '\'' +
        ", createdAt=" + createdAt +
        '}';
  }
}
