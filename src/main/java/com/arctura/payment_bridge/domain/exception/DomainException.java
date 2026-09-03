package com.arctura.payment_bridge.domain.exception;

public class DomainException extends RuntimeException {
  private final String errorCode;
  
  protected DomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return this.errorCode;
  }
}
