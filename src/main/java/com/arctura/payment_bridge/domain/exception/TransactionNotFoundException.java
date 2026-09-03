package com.arctura.payment_bridge.domain.exception;

public class TransactionNotFoundException extends DomainException {
  private static final String CODE = "TRANSACTION_NOT_FOUND";

  public TransactionNotFoundException() {
    super(CODE, "Transaction not found");
  }

  public TransactionNotFoundException(String message) {
    super(CODE, message);
  }
}
