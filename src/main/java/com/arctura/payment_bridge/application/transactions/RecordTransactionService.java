package com.arctura.payment_bridge.application.transactions;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;
import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionRepository;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

@Service
public class RecordTransactionService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public RecordTransactionService(
    AccountRepository accountRepository,
    TransactionRepository transactionRepository
  ) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

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
