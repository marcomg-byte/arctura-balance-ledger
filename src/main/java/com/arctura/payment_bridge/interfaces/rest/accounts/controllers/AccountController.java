package com.arctura.payment_bridge.interfaces.rest.accounts.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.arctura.payment_bridge.domain.exception.AccountNotFoundException;
import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.interfaces.rest.accounts.requests.CreateAccountRequest;
import com.arctura.payment_bridge.interfaces.rest.accounts.requests.UpdatePersonalInfoRequest;
import com.arctura.payment_bridge.interfaces.rest.accounts.responses.AccountResponse;
import com.arctura.payment_bridge.interfaces.rest.exception.request.InvalidAccountIdException;
import com.arctura.payment_bridge.interfaces.rest.exception.request.MissingRequestBodyException;
import com.arctura.payment_bridge.interfaces.rest.exception.request.MissingRequestBodyPropsException;

@RestController
@RequestMapping("/accounts")
public class AccountController {
  private final AccountRepository accountRepository;

  public AccountController(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @PostMapping
  public AccountResponse create(@RequestBody(required = false) CreateAccountRequest request) {
    validateCreateRequest(request);

    Account account = new Account(
      request.name(),
      request.paternalSurname(),
      request.maternalSurname(),
      UUID.randomUUID(),
      new Balance(new Money(request.balanceAmount(), request.balanceCurrency()))
    );

    return AccountResponse.from(this.accountRepository.save(account));
  }

  @GetMapping("/{id}")
  public AccountResponse findById(@PathVariable String id) {
    UUID accountId = parseAccountId(id);

    return this.accountRepository.findById(accountId)
      .map(AccountResponse::from)
      .orElseThrow(AccountNotFoundException::new);
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
  public AccountResponse updatePersonalInfo(
    @PathVariable String id,
    @RequestBody(required = false) UpdatePersonalInfoRequest request
  ) {
    UUID accountId = parseAccountId(id);
    validateUpdateRequest(request);

    Account account = this.accountRepository.findById(accountId)
      .orElseThrow(AccountNotFoundException::new);

    account.updatePersonalInfo(
      request.name(),
      request.paternalSurname(),
      request.maternalSurname()
    );

    return AccountResponse.from(this.accountRepository.save(account));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    UUID accountId = parseAccountId(id);

    if (!this.accountRepository.existsById(accountId)) {
      throw new AccountNotFoundException();
    }

    this.accountRepository.deleteById(accountId);
    return ResponseEntity.noContent().build();
  }

  private UUID parseAccountId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      throw new InvalidAccountIdException(id);
    }
  }

  private void validateCreateRequest(CreateAccountRequest request) {
    if (request == null) {
      throw new MissingRequestBodyException();
    }

    List<String> missingProps = new ArrayList<>();

    addIfBlank(missingProps, request.name(), "name");
    addIfBlank(missingProps, request.paternalSurname(), "paternalSurname");
    addIfBlank(missingProps, request.maternalSurname(), "maternalSurname");

    if (request.balanceAmount() == null) {
      missingProps.add("balanceAmount");
    }

    if (request.balanceCurrency() == null) {
      missingProps.add("balanceCurrency");
    }

    throwIfMissingProps(missingProps);
  }

  private void validateUpdateRequest(UpdatePersonalInfoRequest request) {
    if (request == null) {
      throw new MissingRequestBodyException();
    }

    List<String> missingProps = new ArrayList<>();

    addIfBlank(missingProps, request.name(), "name");
    addIfBlank(missingProps, request.paternalSurname(), "paternalSurname");
    addIfBlank(missingProps, request.maternalSurname(), "maternalSurname");
    throwIfMissingProps(missingProps);
  }

  private void addIfBlank(List<String> missingProps, String value, String propName) {
    if (value == null || value.isBlank()) {
      missingProps.add(propName);
    }
  }

  private void throwIfMissingProps(List<String> missingProps) {
    if (!missingProps.isEmpty()) {
      throw new MissingRequestBodyPropsException(missingProps.toArray(String[]::new));
    }
  }
}
