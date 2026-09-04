package com.arctura.payment_bridge.interfaces.rest.exception.request;

/**
 * Raised when an account path or query parameter cannot be parsed as a UUID.
 *
 * <p>The supplied value is stored as exception detail so API clients can
 * identify the invalid input.</p>
 */
public class InvalidAccountIdException extends RequestException {
  /**
   * Creates the exception for an invalid account id value.
   *
   * @param suppliedId raw id value received from the client
   */
  public InvalidAccountIdException(String suppliedId) {
    super("INVALID_ACCOUNT_ID", "The supplied account id is not a valid UUID", suppliedId);
  }
}
