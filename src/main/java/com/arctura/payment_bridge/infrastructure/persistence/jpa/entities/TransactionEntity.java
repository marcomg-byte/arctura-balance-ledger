package com.arctura.payment_bridge.infrastructure.persistence.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.arctura.payment_bridge.domain.shared.Currency;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class TransactionEntity {
  @Id
  @Column(nullable = false, updatable = false)
  private String id;
  
  @Column(nullable = false, updatable = false)
  private String accountId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Currency currency;

  @Column
  private String description;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  protected TransactionEntity() {}

  public TransactionEntity(
    String id,
    String accountId,
    TransactionType type,
    BigDecimal amount,
    Currency currency,
    String description,
    LocalDateTime createdAt
  ) {
    this.id = id;
    this.accountId = accountId;
    this.type = type;
    this.amount = amount;
    this.currency = currency;
    this.description = description;
    this.createdAt = createdAt;
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

  public void setType(TransactionType type) {
    this.type = type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
