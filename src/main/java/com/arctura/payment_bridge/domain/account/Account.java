package com.arctura.payment_bridge.domain.account;
import com.arctura.payment_bridge.domain.shared.Money;

public class Account {
  private String name;
  private String paternalSurname;
  private String maternalSurname;
  private String id;
  private Balance balance;

  public Account(String name, String paternalSurname, String maternalSurname, String id, Balance balance) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Account id is required");
    }

    if (balance == null) {
      throw new IllegalArgumentException("Balance is required");
    }

    this.balance = balance;
    this.name = name;
    this.paternalSurname = paternalSurname;
    this.maternalSurname = maternalSurname;
    this.id = id;
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

  public String getId() {
    return id;
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
