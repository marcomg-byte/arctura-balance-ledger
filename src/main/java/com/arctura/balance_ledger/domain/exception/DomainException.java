package com.arctura.balance_ledger.domain.exception;

/**
 * Base exception for business rule failures that expose a stable application
 * error code to outer layers.
 *
 * <p>Domain exceptions are framework-independent and are translated into HTTP
 * responses by the REST exception handler.</p>
 */
public class DomainException extends RuntimeException {
  private final String errorCode;
  
  /**
   * Creates a domain exception with an API-stable error code.
   *
   * @param errorCode machine-readable error code
   * @param message human-readable error message
   */
  protected DomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return this.errorCode;
  }
}
