package com.arctura.payment_bridge.infrastructure.persistence.jpa.entities;

import java.math.BigDecimal;
import com.arctura.payment_bridge.domain.shared.Currency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class AccountEntity {
  @Id
  @Column(nullable = false, updatable = false)
  private String id;
  
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String paternalSurname;

  @Column(nullable = false)
  private String maternalSurname;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal balanceAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Currency balanceCurrency;

  protected AccountEntity () {}
  public AccountEntity(
    String id,
    String name,
    String paternalSurname,
    String maternalSurname,
    BigDecimal balanceAmount,
    Currency balanceCurrency
  ) {
    this.id = id;
    this.name = name;
    this.paternalSurname = paternalSurname;
    this.maternalSurname = maternalSurname;
    this.balanceAmount = balanceAmount;
    this.balanceCurrency = balanceCurrency;
  }

  public String getId() {
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
}
