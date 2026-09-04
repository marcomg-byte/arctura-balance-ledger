package com.arctura.payment_bridge.domain.exception;

/**
 * Raised when a transaction cancellation is requested for a ledger entry that
 * already has a cancellation record.
 *
 * <p>This protects the ledger from duplicate reversal entries for the same
 * original transaction.</p>
 */
public class TransactionAlreadyCancelledException extends DomainException {
  private static final String CODE = "TRANSACTION_ALREADY_CANCELLED";

  /**
   * Creates the exception with the default duplicate-cancellation message.
   */
  public TransactionAlreadyCancelledException() {
    super(CODE, "Transaction has already been cancelled");
  }
}
