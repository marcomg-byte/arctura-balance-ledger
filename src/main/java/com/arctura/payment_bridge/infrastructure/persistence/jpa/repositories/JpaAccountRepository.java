package com.arctura.payment_bridge.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.AccountEntity;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers.AccountMapper;

/**
 * Spring Data repository used internally to query account entities, excluding
 * soft-deleted rows from public lookups.
 *
 * <p>The interface stays package-private because application code should depend
 * on the domain repository port rather than Spring Data directly.</p>
 */
interface SpringDataAccountRepository extends JpaRepository<AccountEntity, UUID> {
  /**
   * Finds an active account entity by id.
   *
   * @param id account primary key
   * @return matching active entity, or empty when not found
   */
  Optional<AccountEntity> findByIdAndDeletedAtIsNull(UUID id);

  /**
   * Lists account entities that have not been soft deleted.
   *
   * @return active account entities
   */
  List<AccountEntity> findByDeletedAtIsNull();

  /**
   * Finds active account entities by holder given name.
   *
   * @param name account holder given name
   * @return matching active account entities
   */
  List<AccountEntity> findByNameAndDeletedAtIsNull(String name);

  /**
   * Checks whether an active account entity exists.
   *
   * @param id account primary key
   * @return true when an active entity exists for the id
   */
  boolean existsByIdAndDeletedAtIsNull(UUID id);
}

/**
 * JPA adapter implementing the account domain repository port.
 *
 * <p>The adapter translates between domain aggregates and JPA entities and
 * centralizes the soft-delete behavior for accounts.</p>
 */
@Repository
public class JpaAccountRepository implements AccountRepository {
  private final SpringDataAccountRepository repository;
  private final AccountMapper mapper;

  /**
   * Creates the adapter with its Spring Data repository and mapper.
   *
   * @param repository Spring Data account repository
   * @param mapper account entity/domain mapper
   */
  public JpaAccountRepository(SpringDataAccountRepository repository, AccountMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Persists an account aggregate through JPA.
   *
   * @param account aggregate to save
   * @return saved domain aggregate
   */
  @Override
  public Account save(Account account) {
    return this.mapper.toDomain(repository.save(this.mapper.toEntity(account)));
  }

  /**
   * Finds an active account by id.
   *
   * @param id account identifier
   * @return matching active account, or empty when absent
   */
  @Override
  public Optional<Account> findById(UUID id) {
    return repository.findByIdAndDeletedAtIsNull(id).map(this.mapper::toDomain);
  }

  /**
   * Lists all active accounts.
   *
   * @return active account aggregates
   */
  @Override
  public List<Account> findAll() {
    return repository.findByDeletedAtIsNull()
      .stream()
      .map(this.mapper::toDomain)
      .toList();
  }

  /**
   * Checks whether an active account exists for the supplied id.
   *
   * @param id account identifier
   * @return true when an active account exists
   */
  @Override
  public boolean existsById(UUID id) {
    return repository.existsByIdAndDeletedAtIsNull(id);
  }

  /**
   * Soft-deletes an account by setting its deletion timestamp when the row
   * exists.
   *
   * @param id account identifier
   */
  @Override
  public void deleteById(UUID id) {
    repository.findById(id).ifPresent(account -> {
      account.setDeletedAt(java.time.LocalDateTime.now());
      repository.save(account);
    });
  }

  /**
   * Finds active accounts by holder given name.
   *
   * @param name account holder given name
   * @return matching active account aggregates
   */
  @Override
  public List<Account> findByName(String name) {
    return repository.findByNameAndDeletedAtIsNull(name)
      .stream()
      .map(this.mapper::toDomain)
      .toList();
  }
}
