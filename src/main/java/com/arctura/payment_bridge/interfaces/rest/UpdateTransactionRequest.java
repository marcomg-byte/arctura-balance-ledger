package com.arctura.payment_bridge.interfaces.rest;

import java.math.BigDecimal;

import com.arctura.payment_bridge.domain.shared.Currency;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

public record UpdateTransactionRequest(
  TransactionType type,
  BigDecimal amount,
  Currency currency,
  String description
) {}
