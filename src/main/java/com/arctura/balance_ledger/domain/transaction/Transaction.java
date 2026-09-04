package com.arctura.balance_ledger.domain.transaction;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.balance_ledger.domain.exception.DomainValidationException;
import com.arctura.balance_ledger.domain.shared.Money;

/**
 * Domain aggregate representing a ledger entry.
 *
 * <p>The aggregate enforces the structural invariants for standard operations,
 * transfers, and cancellation records. In particular, transfer transactions
 * require a distinct destination account, cancellation transactions require a
 * reference to the cancelled transaction, and non-cancellation transactions may
 * not carry cancellation metadata.</p>
 */
public class Transaction {
  private final UUID id;
  private final UUID accountId;
  private final UUID destinationAccountId;
  private final UUID cancelledTransactionId;
  private final TransactionType type;
  private final Money amount;
  private String description;
  private final LocalDateTime createdAt;

  /**
   * Creates a transaction with the current time as its creation timestamp.
   *
   * @param id transaction identifier
   * @param accountId source account identifier
   * @param destinationAccountId destination account for transfers, otherwise
   *                             null
   * @param type ledger operation type
   * @param amount transaction amount
   * @param description optional transaction description
   */
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

  /**
   * Creates a cancellation-aware transaction with the current time as its
   * creation timestamp.
   *
   * @param id transaction identifier
   * @param accountId source account identifier
   * @param destinationAccountId destination account for transfer cancellations,
   *                             otherwise null
   * @param cancelledTransactionId transaction being cancelled; required for
   *                               cancellation records
   * @param type ledger operation type
   * @param amount transaction amount
   * @param description optional transaction description
   */
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

  /**
   * Rehydrates or creates a non-cancellation transaction with an explicit
   * creation timestamp.
   *
   * @param id transaction identifier
   * @param accountId source account identifier
   * @param destinationAccountId destination account for transfers, otherwise
   *                             null
   * @param type ledger operation type
   * @param amount transaction amount
   * @param description optional transaction description
   * @param createdAt creation timestamp
   */
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

  /**
   * Rehydrates or creates a transaction with all persisted relationship fields.
   *
   * @param id transaction identifier
   * @param accountId source account identifier
   * @param destinationAccountId destination account for transfers or transfer
   *                             cancellations
   * @param cancelledTransactionId transaction being cancelled for cancellation
   *                               records
   * @param type ledger operation type
   * @param amount transaction amount
   * @param description optional transaction description
   * @param createdAt creation timestamp
   * @throws DomainValidationException when required fields are missing or the
   *                                   transaction relationships are invalid
   */
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

  /**
   * Replaces the mutable transaction description while preserving all immutable
   * ledger values.
   *
   * @param description replacement description, which may be null
   */
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

  /**
   * Builds a diagnostic string for logs and debugging.
   *
   * @return transaction representation including identity, relationships, type,
   *         amount, description, and creation timestamp
   */
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
