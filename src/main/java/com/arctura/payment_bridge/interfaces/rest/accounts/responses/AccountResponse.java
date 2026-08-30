package com.arctura.payment_bridge.interfaces.rest.accounts.responses;

import java.math.BigDecimal;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.shared.Currency;
import com.arctura.payment_bridge.domain.shared.Money;

public record AccountResponse(
  String id,
  String name,
  String paternalSurname,
  String maternalSurname,
  BigDecimal balanceAmount,
  Currency balanceCurrency
) {
  public static AccountResponse from(Account account) {
    Money balance = account.getBalance().getAmount();

    return new AccountResponse(
      account.getId(),
      account.getName(),
      account.getPaternalSurname(),
      account.getMaternalSurname(),
      balance.getAmount(),
      balance.getCurrency()
    );
  }
}
