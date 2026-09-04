package com.arctura.balance_ledger.domain.account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for accounts.
 *
 * <p>Application services depend on this abstraction to load and store active
 * accounts without knowing whether data comes from JPA, an external service, or
 * another persistence mechanism.</p>
 */
public interface AccountRepository {
  /**
   * Persists an account aggregate.
   *
   * @param account aggregate to save
   * @return saved aggregate as reloaded or returned by persistence
   */
  Account save(Account account);

  /**
   * Finds an active account by id.
   *
   * @param id account identifier
   * @return matching account, or empty when not found
   */
  Optional<Account> findById(UUID id);

  /**
   * Lists all active accounts.
   *
   * @return active account aggregates
   */
  List<Account> findAll();

  /**
   * Checks whether an active account exists.
   *
   * @param id account identifier
   * @return true when an active account exists for the id
   */
  boolean existsById(UUID id);

  /**
   * Deletes or deactivates an account by id.
   *
   * @param id account identifier
   */
  void deleteById(UUID id);

  /**
   * Finds active accounts whose given name matches the supplied value.
   *
   * @param name account holder given name
   * @return matching active accounts
   */
  List<Account> findByName(String name);
}
