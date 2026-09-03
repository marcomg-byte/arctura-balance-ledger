package com.arctura.payment_bridge.interfaces.rest.exception.request;

public class MissingRequestBodyException extends RequestException {
  private static final String CODE = "MISSING_REQUEST_BODY";

  public MissingRequestBodyException() {
    super(CODE, "Request body is required but was not provided");
  }
}
