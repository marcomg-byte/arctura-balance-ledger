package com.arctura.payment_bridge.interfaces.rest.exception.request;

public class InvalidAccountIdException extends RequestException {
  public InvalidAccountIdException(String suppliedId) {
    super("INVALID_ACCOUNT_ID", "The supplied account id is not a valid UUID", suppliedId);
  }
}
