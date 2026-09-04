package com.arctura.balance_ledger.interfaces.rest.exception.request;

/**
 * Raised when a transaction path parameter cannot be parsed as a UUID.
 *
 * <p>The supplied value is stored as exception detail so API clients can
 * identify the invalid input.</p>
 */
public class InvalidTransactionIdException extends RequestException {
  /**
   * Creates the exception for an invalid transaction id value.
   *
   * @param suppliedId raw id value received from the client
   */
  public InvalidTransactionIdException(String suppliedId) {
    super("INVALID_TRANSACTION_ID", "The supplied transaction id is not a valid UUID", suppliedId);
  }
}
