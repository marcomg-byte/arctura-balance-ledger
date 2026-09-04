package com.arctura.balance_ledger.interfaces.rest.transactions.requests;

import java.math.BigDecimal;
import java.util.UUID;

import com.arctura.balance_ledger.domain.shared.Currency;
import com.arctura.balance_ledger.domain.transaction.TransactionType;

/**
 * Request payload for creating a ledger transaction, including transfer
 * destination data when required by the transaction type.
 *
 * @param accountId source account identifier
 * @param destinationAccountId destination account identifier for transfers
 * @param type ledger operation to create
 * @param amount transaction amount
 * @param currency transaction currency
 * @param description optional human-readable transaction description
 */
public record CreateTransactionRequest (
  UUID accountId,
  UUID destinationAccountId,
  TransactionType type,
  BigDecimal amount,
  Currency currency,
  String description
) {}
