package com.arctura.payment_bridge.domain.shared;

/**
 * Supported ISO currency codes for account balances and transactions.
 *
 * <p>The enum is persisted by name in JPA entities and serialized by name in
 * the REST API, so renaming values is a data and API compatibility concern.</p>
 */
public enum Currency {
  USD,
  MXN,
  EUR,
}
