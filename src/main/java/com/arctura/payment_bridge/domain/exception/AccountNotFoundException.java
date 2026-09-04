package com.arctura.payment_bridge.domain.exception;

/**
 * Thrown when a requested Account entity cannot be found in the repository.
 *
 * <p>The exception carries a stable domain error code so REST error handling can
 * translate missing active accounts into a consistent API response.</p>
 */
public class AccountNotFoundException extends DomainException {
    private static final String CODE = "ACCOUNT_NOT_FOUND";

    /**
     * Creates the exception with the default account-not-found message.
     */
    public AccountNotFoundException() {
        super(CODE, "Account not found");
    }

    /**
     * Creates the exception with a caller-supplied message while preserving the
     * stable account-not-found error code.
     *
     * @param message message describing the missing account scenario
     */
    public AccountNotFoundException(String message) {
        super(CODE, message);
    }
}
