package com.arctura.payment_bridge.domain.transaction;

/**
 * Ledger operation categories supported by the payment bridge domain.
 *
 * <p>The enum is serialized in the REST API and persisted by name, so changes
 * to value names should be treated as compatibility changes.</p>
 */
public enum TransactionType {
  INCOME,
  EXPENSE,
  TRANSFER,
  CANCEL,
  DEBT_COLLECTION
}
