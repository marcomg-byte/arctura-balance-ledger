package com.arctura.balance_ledger.interfaces.rest.exception.request;

/**
 * Base exception for client request errors that should be rendered as HTTP 400
 * responses with stable API error codes.
 *
 * <p>Controllers throw these exceptions for request parsing and request-shape
 * validation failures before delegating to application services.</p>
 */
public abstract class RequestException extends RuntimeException {
  private final String errorCode;
  private final String detail;

  /**
   * Creates a request exception without additional diagnostic detail.
   *
   * @param errorCode machine-readable API error code
   * @param message human-readable error message
   */
  protected RequestException(String errorCode, String message) {
    this(errorCode, message, null);
  }

  /**
   * Creates a request exception with optional diagnostic detail.
   *
   * @param errorCode machine-readable API error code
   * @param message human-readable error message
   * @param detail optional raw value or comma-separated field list
   */
  protected RequestException(String errorCode, String message, String detail) {
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
