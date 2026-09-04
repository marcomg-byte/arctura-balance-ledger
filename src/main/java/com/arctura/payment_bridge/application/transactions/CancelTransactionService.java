package com.arctura.payment_bridge.application.transactions;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;
import com.arctura.payment_bridge.domain.exception.DomainValidationException;
import com.arctura.payment_bridge.domain.exception.TransactionAlreadyCancelledException;
import com.arctura.payment_bridge.domain.exception.TransactionNotFoundException;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionRepository;
import com.arctura.payment_bridge.domain.transaction.TransactionType;

@Service
public class CancelTransactionService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  public CancelTransactionService(
    AccountRepository accountRepository,
    TransactionRepository transactionRepository
  ) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  @Transactional
  public Transaction cancel(UUID transactionId) {
    Transaction transaction = this.transactionRepository.findById(transactionId)
      .orElseThrow(TransactionNotFoundException::new);

    if (transaction.getType() == TransactionType.CANCEL) {
      throw new DomainValidationException("Cancel transactions cannot be cancelled");
    }

    if (this.transactionRepository.existsByCancelledTransactionId(transactionId)) {
      throw new TransactionAlreadyCancelledException();
    }

    this.reverse(transaction);

    Transaction cancellation = new Transaction(
      UUID.randomUUID(),
      transaction.getAccountId(),
      transaction.getDestinationAccountId(),
      transaction.getId(),
      TransactionType.CANCEL,
      transaction.getAmount(),
      "Cancellation of transaction " + transaction.getId()
    );

    return this.transactionRepository.save(cancellation);
  }

  private void reverse(Transaction transaction) {
    Account account = this.accountRepository.findById(transaction.getAccountId())
      .orElseThrow(AccountNotFoundException::new);
    
    if (transaction.getType() == TransactionType.INCOME) {
      account.decreaseBalance(transaction.getAmount());
      this.accountRepository.save(account);
    }

    if (transaction.getType() == TransactionType.EXPENSE) {
      account.increaseBalance(transaction.getAmount());
      this.accountRepository.save(account);
    }

    if (transaction.getType() == TransactionType.TRANSFER) {
      Account destinationAccount = this.accountRepository.findById(transaction.getDestinationAccountId())
        .orElseThrow(AccountNotFoundException::new);
      
      destinationAccount.decreaseBalance(transaction.getAmount());
      account.increaseBalance(transaction.getAmount());

      this.accountRepository.save(account);
      this.accountRepository.save(destinationAccount);
    }

    if (transaction.getType() == TransactionType.DEBT_COLLECTION) {
      account.increaseBalance(transaction.getAmount());
      this.accountRepository.save(account);
    }
  }
}
