package com.arctura.payment_bridge.domain.exception;

/**
 * Raised when an account balance cannot cover the requested debit operation.
 *
 * <p>The exception represents a business conflict rather than a technical
 * failure, allowing the REST layer to return HTTP 409.</p>
 */
public class InsufficientFundsException extends DomainException {
  private static final String CODE = "INSUFFICIENT_FUNDS";

  /**
   * Creates the exception with the default insufficient-funds message.
   */
  public InsufficientFundsException() {
    super(CODE, "Insufficient funds for the requested operation");
  }

  /**
   * Creates the exception with a caller-supplied message while preserving the
   * stable insufficient-funds error code.
   *
   * @param message message describing the rejected debit
   */
  public InsufficientFundsException(String message) {
    super(CODE, message);
  }
}
