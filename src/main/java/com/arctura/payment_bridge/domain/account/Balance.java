package com.arctura.payment_bridge.domain.account;
import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.exception.InsufficientFundsException;
import com.arctura.payment_bridge.domain.shared.Money;

/**
 * Immutable value object representing an account balance.
 *
 * <p>Balance arithmetic returns new instances and enforces overdraft protection
 * for normal debit operations. Package-private behavior is reserved for domain
 * operations, such as debt collection, that intentionally permit a negative
 * balance.</p>
 */
public class Balance {
  private final Money amount;

  /**
   * Creates a balance from a monetary amount.
   *
   * @param amount monetary value of the balance
   * @throws DomainValidationException when the amount is missing
   */
  public Balance(Money amount) {
    if (amount == null) {
      throw new DomainValidationException("Balance amount is required");
    }

    this.amount = amount;
  }

  /**
   * Returns a new balance increased by the supplied amount.
   *
   * @param money amount to add
   * @return increased balance
   */
  public Balance increaseBy(Money money) {
    return new Balance(this.amount.add(money));
  }

  /**
   * Returns a new balance decreased by the supplied amount.
   *
   * @param money amount to subtract
   * @return decreased balance
   * @throws DomainValidationException when the amount is missing or currency
   *                                   mismatches
   * @throws InsufficientFundsException when the subtraction would make the
   *                                    balance negative
   */
  public Balance decreaseBy(Money money) {
    if (money == null) {
      throw new DomainValidationException("A Money value is required");
    }

    if (money.isGreaterThan(this.amount)) {
      throw new InsufficientFundsException();
    }

    return new Balance(this.amount.subtract(money));
  }

  /**
   * Returns a new balance decreased by the supplied amount, allowing negative
   * balances for debt collection scenarios.
   *
   * @param money amount to subtract
   * @return decreased balance, possibly below zero
   * @throws DomainValidationException when the amount is missing or currency
   *                                   mismatches
   */
  Balance decreaseByAllowingNegative(Money money) {
    if (money == null) {
      throw new DomainValidationException("A money value is required");
    }

    return new Balance(this.amount.subtract(money));
  }

  public Money getAmount() {
    return amount;
  }

  /**
   * Builds a diagnostic string for logs and debugging.
   *
   * @return balance representation including the monetary amount
   */
  @Override
  public String toString() {
    return "Balance{" +
      "amount=" + this.amount +
      '}';
  }
}
