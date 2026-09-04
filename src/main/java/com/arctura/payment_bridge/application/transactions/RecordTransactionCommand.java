package com.arctura.payment_bridge.application.transactions;

import java.util.UUID;

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

/**
 * Application command carrying transaction input into the recording use case.
 *
 * <p>The controller performs request-shape validation before creating this
 * command. Domain validation still runs when the transaction aggregate is
 * created, preserving business rules at the domain boundary.</p>
 *
 * @param accountId source account for the ledger entry
 * @param destinationAccountId destination account for transfers; otherwise null
 * @param type ledger operation to record
 * @param amount monetary value for the operation
 * @param description optional human-readable transaction description
 */
public record RecordTransactionCommand(
  UUID accountId,
  UUID destinationAccountId,
  TransactionType type,
  Money amount,
  String description
) {}
