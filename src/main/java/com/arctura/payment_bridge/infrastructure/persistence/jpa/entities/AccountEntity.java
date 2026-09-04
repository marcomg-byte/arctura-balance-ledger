package com.arctura.payment_bridge.infrastructure.persistence.jpa.entities;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.payment_bridge.domain.shared.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * JPA representation of the accounts table, including persisted balance columns
 * and soft-deletion metadata.
 *
 * <p>This type belongs to the infrastructure layer and should not leak into
 * domain services. Mapping to and from {@link com.arctura.payment_bridge.domain.account.Account}
 * is handled by {@link com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers.AccountMapper}.</p>
 */
@Entity
@Table(name = "accounts")
public class AccountEntity {
  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;
  
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, name = "paternal_surname")
  private String paternalSurname;

  @Column(nullable = false, name = "maternal_surname")
  private String maternalSurname;

  @Column(nullable = false, precision = 19, scale = 2, name = "balance_amount")
  private BigDecimal balanceAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "balance_currency")
  private Currency balanceCurrency;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @OneToMany(mappedBy = "account")
  private List<TransactionEntity> transactions = new ArrayList<>();

  /**
   * Constructor required by JPA for entity materialization.
   */
  protected AccountEntity () {}

  /**
   * Creates an account entity with all persisted account fields.
   *
   * @param id account primary key
   * @param name account holder given name
   * @param paternalSurname account holder paternal surname
   * @param maternalSurname account holder maternal surname
   * @param balanceAmount persisted balance amount
   * @param balanceCurrency persisted balance currency
   * @param deletedAt soft-deletion timestamp, or null for active accounts
   */
  public AccountEntity(
    UUID id,
    String name,
    String paternalSurname,
    String maternalSurname,
    BigDecimal balanceAmount,
    Currency balanceCurrency,
    LocalDateTime deletedAt
  ) {
    this.id = id;
    this.name = name;
    this.paternalSurname = paternalSurname;
    this.maternalSurname = maternalSurname;
    this.balanceAmount = balanceAmount;
    this.balanceCurrency = balanceCurrency;
    this.deletedAt = deletedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPaternalSurname() {
    return paternalSurname;
  }

  public void setPaternalSurname(String paternalSurname) {
    this.paternalSurname = paternalSurname;
  }

  public String getMaternalSurname() {
    return maternalSurname;
  }

  public void setMaternalSurname(String maternalSurname) {
    this.maternalSurname = maternalSurname;
  }

  public BigDecimal getBalanceAmount() {
    return balanceAmount;
  }

  public void setBalanceAmount(BigDecimal balanceAmount) {
    this.balanceAmount = balanceAmount;
  }

  public Currency getBalanceCurrency() {
    return balanceCurrency;
  }

  public void setBalanceCurrency(Currency balanceCurrency) {
    this.balanceCurrency = balanceCurrency;
  }

  public LocalDateTime getDeletedAt() {
    return this.deletedAt;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public List<TransactionEntity> getTransactions() {
    return transactions;
  }
}
