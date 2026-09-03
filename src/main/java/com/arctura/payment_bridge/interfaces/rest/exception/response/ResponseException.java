package com.arctura.payment_bridge.interfaces.rest.exception.response;

public abstract class ResponseException extends RuntimeException {
  private final String errorCode;
  private final String detail;

  protected ResponseException(String errorCode, String message) {
    this(errorCode, message, null);
  }

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
