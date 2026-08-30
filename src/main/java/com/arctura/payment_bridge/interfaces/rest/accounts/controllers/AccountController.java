package com.arctura.payment_bridge.interfaces.rest.accounts.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.domain.account.Balance;
import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.interfaces.rest.accounts.requests.CreateAccountRequest;
import com.arctura.payment_bridge.interfaces.rest.accounts.requests.UpdatePersonalInfoRequest;
import com.arctura.payment_bridge.interfaces.rest.accounts.responses.AccountResponse;

@RestController
@RequestMapping("/accounts")
public class AccountController {
  private final AccountRepository accountRepository;

  public AccountController(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @PostMapping
  public AccountResponse create(@RequestBody CreateAccountRequest request) {
    Account account = new Account(
      request.name(),
      request.paternalSurname(),
      request.maternalSurname(),
      request.id(),
      new Balance(new Money(request.balanceAmount(), request.balanceCurrency()))
    );

    return AccountResponse.from(this.accountRepository.save(account));
  }

  @GetMapping("/{id}")
  public ResponseEntity<AccountResponse> findById(@PathVariable String id) {
      return this.accountRepository.findById(id)
        .map(AccountResponse::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public List<AccountResponse> findAll(@RequestParam(required = false) String name) {
    List<Account> accounts = name == null || name.isBlank()
      ? accountRepository.findAll()
      : accountRepository.findByName(name);

    return accounts.stream()
      .map(AccountResponse::from)
      .toList();
  }
  
  @PatchMapping("/{id}/personal-info")
  public ResponseEntity<AccountResponse> updatePersonalInfo(
    @PathVariable String id,
    @RequestBody UpdatePersonalInfoRequest request
  ) {
    return this.accountRepository.findById(id)
      .map(account -> {
        account.updatePersonalInfo(
          request.name(),
          request.paternalSurname(),
          request.maternalSurname()
        );

        return AccountResponse.from(this.accountRepository.save(account));
      })
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    if (!this.accountRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }

    this.accountRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
