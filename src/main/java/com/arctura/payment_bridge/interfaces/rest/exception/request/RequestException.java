package com.arctura.payment_bridge.interfaces.rest.exception.request;

public abstract class RequestException extends RuntimeException {
  private final String errorCode;
  private final String detail;

  protected RequestException(String errorCode, String message) {
    this(errorCode, message, null);
  }

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
