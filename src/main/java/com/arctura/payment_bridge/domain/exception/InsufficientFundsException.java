package com.arctura.payment_bridge.domain.exception;

public class InsufficientFundsException extends DomainException {
  private static final String CODE = "INSUFFICIENT_FUNDS";

  public InsufficientFundsException() {
    super(CODE, "Insufficient funds for the requested operation");
  }

  public InsufficientFundsException(String message) {
    super(CODE, message);
  }
}
