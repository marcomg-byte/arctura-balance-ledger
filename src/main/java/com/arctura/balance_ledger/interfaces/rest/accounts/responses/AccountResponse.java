package com.arctura.balance_ledger.interfaces.rest.accounts.responses;

import java.math.BigDecimal;
import java.util.UUID;

import com.arctura.balance_ledger.domain.account.Account;
import com.arctura.balance_ledger.domain.shared.Currency;
import com.arctura.balance_ledger.domain.shared.Money;

/**
 * API response projection for account details and balance values.
 *
 * @param id account identifier
 * @param name account holder given name
 * @param paternalSurname account holder paternal surname
 * @param maternalSurname account holder maternal surname
 * @param balanceAmount current balance amount
 * @param balanceCurrency current balance currency
 */
public record AccountResponse(
  UUID id,
  String name,
  String paternalSurname,
  String maternalSurname,
  BigDecimal balanceAmount,
  Currency balanceCurrency
) {
  /**
   * Creates an API response projection from an account aggregate.
   *
   * @param account domain account to project
   * @return account response ready for JSON serialization
   */
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
