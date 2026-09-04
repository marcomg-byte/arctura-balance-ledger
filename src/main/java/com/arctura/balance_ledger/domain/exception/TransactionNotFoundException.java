package com.arctura.balance_ledger.domain.exception;

/**
 * Raised when a requested transaction cannot be found in the transaction
 * repository.
 *
 * <p>The exception carries a stable domain error code so the REST layer can
 * return a consistent not-found response.</p>
 */
public class TransactionNotFoundException extends DomainException {
  private static final String CODE = "TRANSACTION_NOT_FOUND";

  /**
   * Creates the exception with the default transaction-not-found message.
   */
  public TransactionNotFoundException() {
    super(CODE, "Transaction not found");
  }

  /**
   * Creates the exception with a caller-supplied message while preserving the
   * stable transaction-not-found error code.
   *
   * @param message message describing the missing transaction scenario
   */
  public TransactionNotFoundException(String message) {
    super(CODE, message);
  }
}
