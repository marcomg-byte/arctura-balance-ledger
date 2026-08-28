package com.arctura.payment_bridge.domain.account;
import com.arctura.payment_bridge.domain.shared.Money;

public class Balance {
  private final Money amount;

  public Balance(Money amount) {
    this.amount = amount;
  }

  public Balance increaseBy(Money money) {
    return new Balance(this.amount.add(money));
  }

  public Balance decreaseBy(Money money) {
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
