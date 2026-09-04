package com.arctura.payment_bridge.domain.account;
import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.exception.InsufficientFundsException;
import com.arctura.payment_bridge.domain.shared.Money;

public class Balance {
  private final Money amount;

  public Balance(Money amount) {
    if (amount == null) {
      throw new DomainValidationException("Balance amount is required");
    }

    this.amount = amount;
  }

  public Balance increaseBy(Money money) {
    return new Balance(this.amount.add(money));
  }

  public Balance decreaseBy(Money money) {
    if (money == null) {
      throw new DomainValidationException("A Money value is required");
    }

    if (money.isGreaterThan(this.amount)) {
      throw new InsufficientFundsException();
    }

    return new Balance(this.amount.subtract(money));
  }

  Balance decreaseByAllowingNegative(Money money) {
    if (money == null) {
      throw new DomainValidationException("A money value is required");
    }

    return new Balance(this.amount.subtract(money));
  }

  public Money getAmount() {
    return amount;
  }

  @Override
  public String toString() {
    return "Balance{" +
      "amount=" + this.amount +
      '}';
  }
}
