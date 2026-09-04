package com.arctura.balance_ledger.interfaces.rest.accounts.requests;

import java.math.BigDecimal;

import com.arctura.balance_ledger.domain.shared.Currency;

/**
 * Request payload used to create an account with initial personal information
 * and opening balance.
 *
 * @param name account holder given name
 * @param paternalSurname account holder paternal surname
 * @param maternalSurname account holder maternal surname
 * @param balanceAmount initial balance amount
 * @param balanceCurrency currency for the initial balance
 */
public record CreateAccountRequest(
  String name,
  String paternalSurname,
  String maternalSurname,
  BigDecimal balanceAmount,
  Currency balanceCurrency
) {}
