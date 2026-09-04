package com.arctura.balance_ledger.application.transactions;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arctura.balance_ledger.domain.account.Account;
import com.arctura.balance_ledger.domain.account.AccountRepository;
import com.arctura.balance_ledger.domain.exception.AccountNotFoundException;
import com.arctura.balance_ledger.domain.exception.DomainValidationException;
import com.arctura.balance_ledger.domain.transaction.Transaction;
import com.arctura.balance_ledger.domain.transaction.TransactionRepository;
import com.arctura.balance_ledger.domain.transaction.TransactionType;

/**
 * Application service for recording financial transactions.
 *
 * <p>The service owns the use-case transaction boundary: it creates the domain
 * transaction, applies the matching account balance changes, and persists all
 * affected aggregates atomically.</p>
 */
@Service
public class RecordTransactionService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  /**
   * Creates the recording service with the account and transaction repository
   * ports required by the use case.
   *
   * @param accountRepository repository used to load and persist affected
   *                          accounts
   * @param transactionRepository repository used to persist the transaction
   *                              ledger entry
   */
  public RecordTransactionService(
    AccountRepository accountRepository,
    TransactionRepository transactionRepository
  ) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  /**
   * Records a transaction and applies its balance impact.
   *
   * @param command transaction data accepted by the application layer
   * @return persisted transaction aggregate
   * @throws DomainValidationException when attempting to record a cancellation
   *                                   directly or when transaction invariants
   *                                   are violated
   * @throws AccountNotFoundException when the source or transfer destination
   *                                  account does not exist
   */
  @Transactional
  public Transaction record(RecordTransactionCommand command) {
    if (command.type() == TransactionType.CANCEL) {
      throw new DomainValidationException("Cancel transactions can only be created by cancelling an existing transaction");
    }

    Account account = accountRepository.findById(command.accountId())
      .orElseThrow(AccountNotFoundException::new);
    
    Transaction transaction = new Transaction(
      UUID.randomUUID(),
      command.accountId(),
      command.destinationAccountId(),
      command.type(),
      command.amount(),
      command.description()
    );

    if (command.type() == TransactionType.INCOME) {
      account.increaseBalance(command.amount());
      this.accountRepository.save(account);
    }

    if (command.type() == TransactionType.EXPENSE) {
      account.decreaseBalance(command.amount());
      this.accountRepository.save(account);
    }

    if (command.type() == TransactionType.TRANSFER) {
      Account destinationAccount = this.accountRepository.findById(command.destinationAccountId())
        .orElseThrow(AccountNotFoundException::new);

      account.decreaseBalance(command.amount());
      destinationAccount.increaseBalance(command.amount());

      accountRepository.save(account);
      accountRepository.save(destinationAccount);
    }

    if (command.type() == TransactionType.DEBT_COLLECTION) {
      account.collectDebt(command.amount());
      this.accountRepository.save(account);
    }

    return transactionRepository.save(transaction);
  }
}
