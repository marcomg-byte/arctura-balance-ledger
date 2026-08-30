package com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.Balance;
import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.AccountEntity;

@Component
public class AccountMapper {
  public AccountEntity toEntity(Account account) {
    Money balance = account.getBalance().getAmount();
    
    return new AccountEntity(
      account.getId(),
      account.getName(),
      account.getPaternalSurname(),
      account.getMaternalSurname(),
      balance.getAmount(),
      balance.getCurrency()
    );
  }

  public Account toDomain(AccountEntity entity) {
    Money money = new Money(entity.getBalanceAmount(), entity.getBalanceCurrency());
    Balance balance = new Balance(money);

    return new Account(
      entity.getName(),
      entity.getPaternalSurname(),
      entity.getMaternalSurname(),
      entity.getId(),
      balance
    );
  }
}
