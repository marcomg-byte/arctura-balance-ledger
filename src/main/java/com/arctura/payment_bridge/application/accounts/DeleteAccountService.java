package com.arctura.payment_bridge.application.accounts;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;

@Service
public class DeleteAccountService {
  private final AccountRepository accountRepository;

  public DeleteAccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Transactional
  public void delete(UUID accountId) {
    if (!this.accountRepository.existsById(accountId)) {
      throw new AccountNotFoundException();
    }

    this.accountRepository.deleteById(accountId);
  }
}
