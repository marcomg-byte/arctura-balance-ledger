package com.arctura.payment_bridge.domain.shared;
import java.math.BigDecimal;
import java.util.Objects;

import com.arctura.payment_bridge.domain.exception.DomainValidationException;

public class Money {
  private final BigDecimal amount;
  private final Currency currency;

  public Money(BigDecimal amount, Currency currency) {
    if (amount == null) {
      throw new DomainValidationException("Amount is required");
    }

    if (currency == null) {
      throw new DomainValidationException("Currency is required");
    }

    this.amount = amount;
    this.currency = currency;
  }

  public Money add(Money newAmount) {
    this.validateSameCurrency(newAmount);
    return new Money(this.amount.add(newAmount.amount), this.currency);
  }

  public Money subtract(Money newAmount) {
    this.validateSameCurrency(newAmount);
    return new Money(this.amount.subtract(newAmount.amount), this.currency);
  }

  public boolean isGreaterThan(Money value) {
    this.validateSameCurrency(value);
    return this.amount.compareTo(value.amount) > 0;
  }
  
  public BigDecimal getAmount() {
    return amount;
  }

  public Currency getCurrency() {
    return currency;
  }

  private void validateSameCurrency(Money value) {
    if (value == null) {
      throw new DomainValidationException("A Money value is required");
    }

    if (this.currency != value.currency) {
      throw new DomainValidationException("Money currency must match");
    }
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof Money money)) {
      return false;
    }

    return amount.compareTo(money.amount) == 0 && currency == money.currency;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.amount.stripTrailingZeros(), this.currency);
  }

  @Override
  public String toString() {
    return this.amount + " " + this.currency;
  }
}
