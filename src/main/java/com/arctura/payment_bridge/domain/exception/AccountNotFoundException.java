package com.arctura.payment_bridge.domain.exception;

/**
 * Thrown when a requested Account entity cannot be found in the repository.
 */
public class AccountNotFoundException extends DomainException {
    private static final String CODE = "ACCOUNT_NOT_FOUND";

    public AccountNotFoundException() {
        super(CODE, "Account not found");
    }

    public AccountNotFoundException(String message) {
        super(CODE, message);
    }
}
