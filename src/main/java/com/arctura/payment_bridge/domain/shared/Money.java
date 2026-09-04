package com.arctura.payment_bridge.domain.shared;
import java.math.BigDecimal;
import java.util.Objects;

import com.arctura.payment_bridge.domain.exception.DomainValidationException;

/**
 * Immutable monetary value object.
 *
 * <p>All arithmetic methods require matching currencies before operating on the
 * numeric amount. Equality compares numeric value with {@link BigDecimal}
 * scale-insensitive semantics, so {@code 10.0 USD} and {@code 10.00 USD} are
 * treated as the same money value.</p>
 */
public class Money {
  private final BigDecimal amount;
  private final Currency currency;

  /**
   * Creates a monetary value.
   *
   * @param amount numeric amount
   * @param currency currency for the amount
   * @throws DomainValidationException when amount or currency is missing
   */
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

  /**
   * Adds another money value with the same currency.
   *
   * @param newAmount amount to add
   * @return sum as a new money value
   * @throws DomainValidationException when the other value is missing or has a
   *                                   different currency
   */
  public Money add(Money newAmount) {
    this.validateSameCurrency(newAmount);
    return new Money(this.amount.add(newAmount.amount), this.currency);
  }

  /**
   * Subtracts another money value with the same currency.
   *
   * @param newAmount amount to subtract
   * @return difference as a new money value
   * @throws DomainValidationException when the other value is missing or has a
   *                                   different currency
   */
  public Money subtract(Money newAmount) {
    this.validateSameCurrency(newAmount);
    return new Money(this.amount.subtract(newAmount.amount), this.currency);
  }

  /**
   * Compares this amount with another value in the same currency.
   *
   * @param value value to compare against
   * @return true when this value is numerically greater than the supplied value
   * @throws DomainValidationException when the other value is missing or has a
   *                                   different currency
   */
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

  /**
   * Verifies that another money value exists and uses the same currency.
   *
   * @param value value to compare against this instance
   * @throws DomainValidationException when the value is missing or uses another
   *                                   currency
   */
  private void validateSameCurrency(Money value) {
    if (value == null) {
      throw new DomainValidationException("A Money value is required");
    }

    if (this.currency != value.currency) {
      throw new DomainValidationException("Money currency must match");
    }
  }

  /**
   * Compares money values using scale-insensitive amount equality and exact
   * currency equality.
   *
   * @param object object to compare with this money value
   * @return true when both amount and currency represent the same value
   */
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

  /**
   * Computes a hash code aligned with scale-insensitive monetary equality.
   *
   * @return hash code for normalized amount and currency
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.amount.stripTrailingZeros(), this.currency);
  }

  /**
   * Formats the money value for diagnostics.
   *
   * @return amount followed by currency code
   */
  @Override
  public String toString() {
    return this.amount + " " + this.currency;
  }
}
