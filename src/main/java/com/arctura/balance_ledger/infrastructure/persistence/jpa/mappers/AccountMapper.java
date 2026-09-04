package com.arctura.balance_ledger.infrastructure.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.arctura.balance_ledger.domain.account.Account;
import com.arctura.balance_ledger.domain.account.Balance;
import com.arctura.balance_ledger.domain.shared.Money;
import com.arctura.balance_ledger.infrastructure.persistence.jpa.entities.AccountEntity;

/**
 * Converts accounts between the persistence entity shape and the domain
 * aggregate shape used by application services.
 *
 * <p>The mapper keeps JPA annotations and column details out of the domain
 * model while preserving balance and soft-deletion state.</p>
 */
@Component
public class AccountMapper {
  /**
   * Converts a domain account into a JPA entity ready for persistence.
   *
   * @param account domain aggregate to convert
   * @return account entity containing persisted field values
   */
  public AccountEntity toEntity(Account account) {
    Money balance = account.getBalance().getAmount();
    
    return new AccountEntity(
      account.getId(),
      account.getName(),
      account.getPaternalSurname(),
      account.getMaternalSurname(),
      balance.getAmount(),
      balance.getCurrency(),
      account.getDeletedAt()
    );
  }

  /**
   * Converts a JPA account entity into a domain aggregate.
   *
   * @param entity persisted account entity to convert
   * @return domain account aggregate
   */
  public Account toDomain(AccountEntity entity) {
    Money money = new Money(entity.getBalanceAmount(), entity.getBalanceCurrency());
    Balance balance = new Balance(money);

    return new Account(
      entity.getName(),
      entity.getPaternalSurname(),
      entity.getMaternalSurname(),
      entity.getId(),
      balance,
      entity.getDeletedAt()
    );
  }
}
