package com.arctura.payment_bridge.application.accounts;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;

/**
 * Application service for account deletion workflows.
 *
 * <p>The service validates that the account exists through the domain repository
 * and delegates the actual deletion behavior to the persistence adapter, which
 * currently implements account deletion as a soft delete.</p>
 */
@Service
public class DeleteAccountService {
  private final AccountRepository accountRepository;

  /**
   * Creates the service with the account repository port used by the application
   * layer.
   *
   * @param accountRepository repository used to check account existence and
   *                          delete accounts
   */
  public DeleteAccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  /**
   * Deletes an account when it exists.
   *
   * @param accountId identifier of the account to delete
   * @throws AccountNotFoundException when no active account exists for the id
   */
  @Transactional
  public void delete(UUID accountId) {
    if (!this.accountRepository.existsById(accountId)) {
      throw new AccountNotFoundException();
    }

    this.accountRepository.deleteById(accountId);
  }
}
