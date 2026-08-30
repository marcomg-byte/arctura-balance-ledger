package com.arctura.payment_bridge.interfaces.rest.accounts.requests;

import java.math.BigDecimal;

import com.arctura.payment_bridge.domain.shared.Currency;

public record CreateAccountRequest(
  String id,
  String name,
  String paternalSurname,
  String maternalSurname,
  BigDecimal balanceAmount,
  Currency balanceCurrency
) {}
