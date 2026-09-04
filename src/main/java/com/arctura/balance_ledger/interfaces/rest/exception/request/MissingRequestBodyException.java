package com.arctura.balance_ledger.interfaces.rest.exception.request;

/**
 * Raised when an endpoint requires a JSON request body but receives none.
 *
 * <p>This is distinct from malformed JSON so clients can differentiate absent
 * payloads from invalid payloads.</p>
 */
public class MissingRequestBodyException extends RequestException {
  private static final String CODE = "MISSING_REQUEST_BODY";

  /**
   * Creates the exception with the standard missing-body error message.
   */
  public MissingRequestBodyException() {
    super(CODE, "Request body is required but was not provided");
  }
}
