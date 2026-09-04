package com.arctura.payment_bridge.domain.account;
import java.time.LocalDateTime;
import java.util.UUID;

import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.shared.Money;

public class Account {
  private String name;
  private String paternalSurname;
  private String maternalSurname;
  private UUID id;
  private Balance balance;
  private LocalDateTime deletedAt;

  public Account(String name, String paternalSurname, String maternalSurname, UUID id, Balance balance) {
    this(name, paternalSurname, maternalSurname, id, balance, null);
  }

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

  public void increaseBalance(Money amount) {
    this.balance = this.balance.increaseBy(amount);
  }

  public void decreaseBalance(Money amount) {
    this.balance = this.balance.decreaseBy(amount);
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

  public void delete() {
    if (this.deletedAt == null) {
      this.deletedAt = LocalDateTime.now();
    }
  }

  public void updatePersonalInfo(String name, String paternalSurname, String maternalSurname) {
    this.name = name;
    this.paternalSurname = paternalSurname;
    this.maternalSurname = maternalSurname;
  }

  public String getPersonalInfo() {
    return this.name + " " + this.paternalSurname + " " + this.maternalSurname;
  }

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
