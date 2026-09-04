package com.arctura.payment_bridge.domain.exception;

public class TransactionAlreadyCancelledException extends DomainException {
  private static final String CODE = "TRANSACTION_ALREADY_CANCELLED";

  public TransactionAlreadyCancelledException() {
    super(CODE, "Transaction has already been cancelled");
  }
}
