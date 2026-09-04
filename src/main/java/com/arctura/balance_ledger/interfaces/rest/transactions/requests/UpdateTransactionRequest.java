package com.arctura.balance_ledger.interfaces.rest.transactions.requests;

import java.math.BigDecimal;

import com.arctura.balance_ledger.domain.shared.Currency;
import com.arctura.balance_ledger.domain.transaction.TransactionType;

/**
 * Request payload for updating mutable transaction fields while detecting
 * attempts to change read-only ledger values.
 *
 * @param type read-only transaction type, rejected when supplied
 * @param amount read-only transaction amount, rejected when supplied
 * @param currency read-only transaction currency, rejected when supplied
 * @param description replacement transaction description
 */
public record UpdateTransactionRequest(
  TransactionType type,
  BigDecimal amount,
  Currency currency,
  String description
) {}
