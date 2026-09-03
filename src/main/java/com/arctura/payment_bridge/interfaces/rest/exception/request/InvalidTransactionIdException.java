package com.arctura.payment_bridge.interfaces.rest.exception.request;

public class InvalidTransactionIdException extends RequestException {
  public InvalidTransactionIdException(String suppliedId) {
    super("INVALID_TRANSACTION_ID", "The supplied transaction id is not a valid UUID", suppliedId);
  }
}
