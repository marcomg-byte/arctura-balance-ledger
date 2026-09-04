package com.arctura.payment_bridge.application.transactions;

import java.util.UUID;

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

public record RecordTransactionCommand(
  UUID accountId,
  UUID destinationAccountId,
  TransactionType type,
  Money amount,
  String description
) {}
