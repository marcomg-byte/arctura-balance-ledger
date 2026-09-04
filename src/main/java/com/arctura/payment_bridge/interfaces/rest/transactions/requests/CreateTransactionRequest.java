package com.arctura.payment_bridge.interfaces.rest.transactions.requests;

import java.math.BigDecimal;
import java.util.UUID;

import com.arctura.payment_bridge.domain.shared.Currency;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

public record CreateTransactionRequest (
  UUID accountId,
  UUID destinationAccountId,
  TransactionType type,
  BigDecimal amount,
  Currency currency,
  String description
) {}
