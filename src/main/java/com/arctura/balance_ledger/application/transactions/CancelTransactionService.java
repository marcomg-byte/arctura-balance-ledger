package com.arctura.balance_ledger.application.transactions;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arctura.balance_ledger.domain.account.Account;
import com.arctura.balance_ledger.domain.account.AccountRepository;
import com.arctura.balance_ledger.domain.exception.AccountNotFoundException;
import com.arctura.balance_ledger.domain.exception.DomainValidationException;
import com.arctura.balance_ledger.domain.exception.TransactionAlreadyCancelledException;
import com.arctura.balance_ledger.domain.exception.TransactionNotFoundException;
import com.arctura.balance_ledger.domain.transaction.Transaction;
import com.arctura.balance_ledger.domain.transaction.TransactionRepository;
import com.arctura.balance_ledger.domain.transaction.TransactionType;

/**
 * Application service for transaction cancellation workflows.
 *
 * <p>Cancellation is modeled as a new ledger transaction rather than mutating or
 * deleting the original entry. The original balance impact is reversed inside a
 * transaction and the cancellation record points back to the cancelled
 * transaction for auditability.</p>
 */
@Service
public class CancelTransactionService {
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;

  /**
   * Creates the cancellation service with the domain repository ports required
   * to load accounts and transactions.
   *
   * @param accountRepository repository used to load and persist affected
   *                          accounts
   * @param transactionRepository repository used to load, validate, and persist
   *                              transaction records
   */
  public CancelTransactionService(
    AccountRepository accountRepository,
    TransactionRepository transactionRepository
  ) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
  }

  /**
   * Cancels an existing transaction and returns the generated cancellation
   * transaction.
   *
   * @param transactionId identifier of the original transaction to cancel
   * @return persisted cancellation transaction
   * @throws TransactionNotFoundException when the original transaction does not
   *                                      exist
   * @throws DomainValidationException when the supplied transaction is itself a
   *                                   cancellation
   * @throws TransactionAlreadyCancelledException when the transaction already
   *                                             has a cancellation record
   */
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

  /**
   * Applies the inverse balance movement for the supplied transaction.
   *
   * @param transaction transaction whose ledger effect must be reversed
   * @throws AccountNotFoundException when an affected source or destination
   *                                  account no longer exists
   */
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
