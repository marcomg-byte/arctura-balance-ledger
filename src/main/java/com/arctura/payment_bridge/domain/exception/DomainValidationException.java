package com.arctura.payment_bridge.domain.exception;

/**
 * Raised when a domain object or operation violates required business
 * invariants.
 *
 * <p>Use this exception for invalid aggregate construction or unsupported
 * domain operations where no more specific domain exception exists.</p>
 */
public class DomainValidationException extends DomainException {
  private static final String CODE = "DOMAIN_VALIDATION_ERROR";

  /**
   * Creates the exception with a specific domain validation message.
   *
   * @param message message describing the violated invariant
   */
  public DomainValidationException(String message) {
    super(CODE, message);
  }
}
