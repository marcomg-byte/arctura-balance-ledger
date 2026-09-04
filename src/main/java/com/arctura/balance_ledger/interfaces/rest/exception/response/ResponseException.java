package com.arctura.balance_ledger.interfaces.rest.exception.response;

/**
 * Base exception for API response errors that carry public error codes and
 * optional diagnostic details.
 *
 * <p>These exceptions represent interface-layer failures that are not domain
 * business rules but still need consistent error payloads.</p>
 */
public abstract class ResponseException extends RuntimeException {
  private final String errorCode;
  private final String detail;

  /**
   * Creates a response exception without additional diagnostic detail.
   *
   * @param errorCode machine-readable API error code
   * @param message human-readable error message
   */
  protected ResponseException(String errorCode, String message) {
    this(errorCode, message, null);
  }

  /**
   * Creates a response exception with optional diagnostic detail.
   *
   * @param errorCode machine-readable API error code
   * @param message human-readable error message
   * @param detail optional detail value included in the API error model
   */
  public ResponseException(String errorCode, String message, String detail) {
    super(message);
    this.errorCode = errorCode;
    this.detail = detail;
  }

  public String getErrorCode() {
    return this.errorCode;
  }
  
  public String getDetail() {
    return this.detail;
  }
}
