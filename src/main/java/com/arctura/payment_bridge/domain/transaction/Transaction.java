package com.arctura.payment_bridge.domain.transaction;
import java.time.LocalDateTime;
import com.arctura.payment_bridge.domain.shared.Money;

public class Transaction {
  private final String id;
  private final String accountId;
  private TransactionType type;
  private Money amount;
  private String description;
  private final LocalDateTime createdAt;

  public Transaction(
    String id,
    String accountId,
    TransactionType type,
    Money amount,
    String description
  ) {
    this(id, accountId, type, amount, description, LocalDateTime.now());
  }

  public Transaction(
    String id,
    String accountId,
    TransactionType type,
    Money amount,
    String description,
    LocalDateTime createdAt
  ) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Transaction id is required");
    }

    if (accountId == null || accountId.isBlank()) {
      throw new IllegalArgumentException("Account id is required");
    }

    if (type == null) {
      throw new IllegalArgumentException("Transaction type is required");
    }

    if (amount == null) {
      throw new IllegalArgumentException("Transaction amount is required");
    }

    if (createdAt == null) {
      throw new IllegalArgumentException("Transaction creation date is required");
    }

    this.id = id;
    this.accountId = accountId;
    this.type = type;
    this.amount = amount;
    this.description = description;
    this.createdAt = createdAt;
  }

  public void update(TransactionType type, Money amount, String description) {
    if (type == null) {
      throw new IllegalArgumentException("Transaction type is required");
    }

    if (amount == null) {
      throw new IllegalArgumentException("Transaction amount is required");
    }

    this.type = type;
    this.amount = amount;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public String getAccountId() {
    return accountId;
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
        ", type=" + type +
        ", amount=" + amount +
        ", description='" + description + '\'' +
        ", createdAt=" + createdAt +
        '}';
  }
}
