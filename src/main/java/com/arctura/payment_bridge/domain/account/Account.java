package com.arctura.payment_bridge.domain.account;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.shared.Money;

/**
 * Domain aggregate representing a customer account.
 *
 * <p>The aggregate owns personal information, current balance, and soft-deletion
 * state. Balance-changing methods delegate arithmetic and overdraft protection
 * to {@link Balance}, keeping account behavior expressed in domain terms.</p>
 */
public class Account {
  private String name;
  private String paternalSurname;
  private String maternalSurname;
  private UUID id;
  private Balance balance;
  private LocalDateTime deletedAt;

  /**
   * Creates an active account.
   *
   * @param name account holder given name
   * @param paternalSurname account holder paternal surname
   * @param maternalSurname account holder maternal surname
   * @param id domain identifier for the account
   * @param balance initial account balance
   */
  public Account(String name, String paternalSurname, String maternalSurname, UUID id, Balance balance) {
    this(name, paternalSurname, maternalSurname, id, balance, null);
  }

  /**
   * Creates an account with an explicit soft-deletion state.
   *
   * @param name account holder given name
   * @param paternalSurname account holder paternal surname
   * @param maternalSurname account holder maternal surname
   * @param id domain identifier for the account
   * @param balance current account balance
   * @param deletedAt timestamp marking soft deletion, or null when active
   * @throws DomainValidationException when the id or balance is missing
   */
  public Account(
    String name,
    String paternalSurname,
    String maternalSurname,
    UUID id,
    Balance balance,
    LocalDateTime deletedAt
  ) {
    if (id == null) {
      throw new DomainValidationException("Account id is required");
    }

    if (balance == null) {
      throw new DomainValidationException("Balance is required");
    }

    this.balance = balance;
    this.name = name;
    this.paternalSurname = paternalSurname;
    this.maternalSurname = maternalSurname;
    this.id = id;
    this.deletedAt = deletedAt;
  }

  /**
   * Credits the account balance.
   *
   * @param amount amount to add to the balance
   */
  public void increaseBalance(Money amount) {
    this.balance = this.balance.increaseBy(amount);
  }

  /**
   * Debits the account balance using standard overdraft protection.
   *
   * @param amount amount to subtract from the balance
   */
  public void decreaseBalance(Money amount) {
    this.balance = this.balance.decreaseBy(amount);
  }

  /**
   * Applies a debt collection debit that is allowed to take the balance below
   * zero.
   *
   * @param amount amount to collect from the account
   */
  public void collectDebt(Money amount) {
    this.balance = this.balance.decreaseByAllowingNegative(amount);
  }

  public Balance getBalance() {
    return balance;
  }

  public String getName() {
    return name;
  }

  public String getPaternalSurname() {
    return paternalSurname;
  }

  public String getMaternalSurname() {
    return maternalSurname;
  }

  public UUID getId() {
    return id;
  }

  public LocalDateTime getDeletedAt() {
    return this.deletedAt;
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }

  /**
   * Marks the account as deleted without removing historical ledger records.
   *
   * <p>The operation is idempotent: once the deletion timestamp is set, repeated
   * calls leave the original timestamp intact.</p>
   */
  public void delete() {
    if (this.deletedAt == null) {
      this.deletedAt = LocalDateTime.now();
    }
  }

  /**
   * Replaces the account holder's personal information.
   *
   * @param name new given name
   * @param paternalSurname new paternal surname
   * @param maternalSurname new maternal surname
   */
  public void updatePersonalInfo(String name, String paternalSurname, String maternalSurname) {
    this.name = name;
    this.paternalSurname = paternalSurname;
    this.maternalSurname = maternalSurname;
  }

  public String getPersonalInfo() {
    return this.name + " " + this.paternalSurname + " " + this.maternalSurname;
  }

  /**
   * Builds a diagnostic string for logs and debugging.
   *
   * @return account representation including identity, names, and balance
   */
  @Override
  public String toString() {
    return "Account{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", paternalSurname='" + paternalSurname + '\'' +
      ", maternalSurname='" + maternalSurname + '\'' +
      ", balance=" + balance +
      '}';
  }
}
