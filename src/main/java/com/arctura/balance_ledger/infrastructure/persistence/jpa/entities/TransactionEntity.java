package com.arctura.balance_ledger.infrastructure.persistence.jpa.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.balance_ledger.domain.shared.Currency;
import com.arctura.balance_ledger.domain.transaction.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA representation of ledger transactions and their account, destination, and
 * cancellation relationships.
 *
 * <p>This entity stores database relationships as lazy JPA associations while
 * exposing ids to the mapper so the domain transaction can remain persistence
 * agnostic.</p>
 */
@Entity
@Table(name = "transactions")
public class TransactionEntity {
  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;
  
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountEntity account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "destination_account_id")
  private AccountEntity destinationAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cancelled_transaction_id")
  private TransactionEntity cancelledTransaction;

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

  @Column(nullable = false, updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  /**
   * Constructor required by JPA for entity materialization.
   */
  protected TransactionEntity() {}

  /**
   * Creates a transaction entity with all persisted ledger fields and
   * relationships.
   *
   * @param id transaction primary key
   * @param account source account relationship
   * @param destinationAccount destination account relationship for transfers
   * @param cancelledTransaction cancelled transaction relationship for
   *                             cancellation records
   * @param type persisted transaction type
   * @param amount persisted monetary amount
   * @param currency persisted currency
   * @param description persisted description
   * @param createdAt creation timestamp
   */
  public TransactionEntity(
    UUID id,
    AccountEntity account,
    AccountEntity destinationAccount,
    TransactionEntity cancelledTransaction,
    TransactionType type,
    BigDecimal amount,
    Currency currency,
    String description,
    LocalDateTime createdAt
  ) {
    this.id = id;
    this.account = account;
    this.destinationAccount = destinationAccount;
    this.cancelledTransaction = cancelledTransaction;
    this.type = type;
    this.amount = amount;
    this.currency = currency;
    this.description = description;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return account.getId();
  }

  public UUID getDestinationAccountId() {
    return destinationAccount == null ? null : destinationAccount.getId();
  }

  public UUID getCancelledTransactionId() {
    return cancelledTransaction == null ? null : cancelledTransaction.getId();
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
