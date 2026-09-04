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

import com.arctura.payment_bridge.application.accounts.DeleteAccountService;
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

/**
 * REST controller exposing account operations.
 *
 * <p>The controller is responsible for HTTP request parsing and request-shape
 * validation. Domain behavior is delegated to repositories and application
 * services so that the web layer stays focused on API concerns.</p>
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {
  private final AccountRepository accountRepository;
  private final DeleteAccountService deleteAccountService;

  /**
   * Creates the controller with the dependencies required for account API
   * operations.
   *
   * @param accountRepository repository port used for account persistence and
   *                          lookup
   * @param deleteAccountService use case service that handles deletion rules
   */
  public AccountController(AccountRepository accountRepository, DeleteAccountService deleteAccountService) {
    this.accountRepository = accountRepository;
    this.deleteAccountService = deleteAccountService;
  }

  /**
   * Creates a new account from a JSON request body.
   *
   * @param request account creation payload, required by this endpoint
   * @return serialized account response for the saved account
   * @throws MissingRequestBodyException when the body is absent
   * @throws MissingRequestBodyPropsException when required fields are missing
   */
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

  /**
   * Finds a single active account by UUID path parameter.
   *
   * @param id raw account id path parameter
   * @return serialized account response
   * @throws InvalidAccountIdException when the path parameter is not a UUID
   * @throws AccountNotFoundException when no active account exists for the id
   */
  @GetMapping("/{id}")
  public AccountResponse findById(@PathVariable String id) {
    UUID accountId = parseAccountId(id);

    return this.accountRepository.findById(accountId)
      .map(AccountResponse::from)
      .orElseThrow(AccountNotFoundException::new);
  }

  /**
   * Lists active accounts, optionally filtering by the account holder's given
   * name.
   *
   * @param name optional account holder given-name filter
   * @return serialized account responses
   */
  @GetMapping
  public List<AccountResponse> findAll(@RequestParam(required = false) String name) {
    List<Account> accounts = name == null || name.isBlank()
      ? accountRepository.findAll()
      : accountRepository.findByName(name);

    return accounts.stream()
      .map(AccountResponse::from)
      .toList();
  }
  
  /**
   * Replaces the personal information for an active account.
   *
   * @param id raw account id path parameter
   * @param request personal information replacement payload
   * @return serialized account response after persistence
   * @throws InvalidAccountIdException when the path parameter is not a UUID
   * @throws MissingRequestBodyException when the body is absent
   * @throws MissingRequestBodyPropsException when required fields are missing
   * @throws AccountNotFoundException when no active account exists for the id
   */
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

  /**
   * Soft-deletes an active account.
   *
   * @param id raw account id path parameter
   * @return empty HTTP 204 response when deletion succeeds
   * @throws InvalidAccountIdException when the path parameter is not a UUID
   * @throws AccountNotFoundException when no active account exists for the id
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    UUID accountId = parseAccountId(id);

    this.deleteAccountService.delete(accountId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Parses an account id from a URL or query-string value.
   *
   * @param id raw id value supplied by the client
   * @return parsed account UUID
   * @throws InvalidAccountIdException when the value is not a UUID
   */
  private UUID parseAccountId(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException exception) {
      throw new InvalidAccountIdException(id);
    }
  }

  /**
   * Validates required properties for account creation.
   *
   * @param request request body to validate
   * @throws MissingRequestBodyException when the request body is absent
   * @throws MissingRequestBodyPropsException when required properties are absent
   *                                         or blank
   */
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

  /**
   * Validates required properties for personal information updates.
   *
   * @param request request body to validate
   * @throws MissingRequestBodyException when the request body is absent
   * @throws MissingRequestBodyPropsException when required properties are absent
   *                                         or blank
   */
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

  /**
   * Adds a property name to the missing-property list when the supplied string
   * is null or blank.
   *
   * @param missingProps mutable list collecting missing property names
   * @param value property value to inspect
   * @param propName property name to report when missing
   */
  private void addIfBlank(List<String> missingProps, String value, String propName) {
    if (value == null || value.isBlank()) {
      missingProps.add(propName);
    }
  }

  /**
   * Raises a request validation exception when any required properties are
   * missing.
   *
   * @param missingProps missing request property names
   * @throws MissingRequestBodyPropsException when the list is not empty
   */
  private void throwIfMissingProps(List<String> missingProps) {
    if (!missingProps.isEmpty()) {
      throw new MissingRequestBodyPropsException(missingProps.toArray(String[]::new));
    }
  }
}
