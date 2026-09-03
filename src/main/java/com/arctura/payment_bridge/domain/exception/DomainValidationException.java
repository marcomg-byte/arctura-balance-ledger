package com.arctura.payment_bridge.domain.exception;

public class DomainValidationException extends DomainException {
  private static final String CODE = "DOMAIN_VALIDATION_ERROR";

  public DomainValidationException(String message) {
    super(CODE, message);
  }
}
