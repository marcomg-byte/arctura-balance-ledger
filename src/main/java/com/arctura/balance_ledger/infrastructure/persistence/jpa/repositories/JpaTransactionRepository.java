package com.arctura.balance_ledger.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arctura.balance_ledger.domain.transaction.Transaction;
import com.arctura.balance_ledger.domain.transaction.TransactionRepository;
import com.arctura.balance_ledger.infrastructure.persistence.jpa.entities.TransactionEntity;
import com.arctura.balance_ledger.infrastructure.persistence.jpa.mappers.TransactionMapper;

/**
 * Spring Data repository used internally for transaction persistence and
 * derived transaction relationship queries.
 *
 * <p>The interface stays package-private so application code depends on the
 * domain repository port instead of Spring Data.</p>
 */
interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
  /**
   * Finds transactions where the supplied account is either the source account
   * or destination account.
   *
   * @param accountId source account id to match
   * @param destinationAccountId destination account id to match
   * @return transaction entities involving the account
   */
  List<TransactionEntity> findByAccount_IdOrDestinationAccount_Id(
    UUID accountId,
    UUID destinationAccountId
  );

  /**
   * Checks whether any cancellation transaction points at the supplied original
   * transaction.
   *
   * @param cancelledTransactionId original transaction id
   * @return true when a cancellation relationship already exists
   */
  boolean existsByCancelledTransaction_Id(UUID cancelledTransactionId);
}

/**
 * JPA adapter implementing the transaction domain repository port.
 *
 * <p>The adapter translates JPA entities into domain aggregates and provides the
 * query operations required by ledger recording and cancellation use cases.</p>
 */
@Repository
public class JpaTransactionRepository implements TransactionRepository {
  private final SpringDataTransactionRepository repository;
  private final TransactionMapper mapper;

  /**
   * Creates the adapter with its Spring Data repository and mapper.
   *
   * @param repository Spring Data transaction repository
   * @param mapper transaction entity/domain mapper
   */
  public JpaTransactionRepository(
    SpringDataTransactionRepository repository,
    TransactionMapper mapper
  ) {
    this.repository = repository;
    this.mapper = mapper;
  }

  /**
   * Persists a transaction aggregate through JPA.
   *
   * @param transaction aggregate to save
   * @return saved domain aggregate
   */
  @Override
  public Transaction save(Transaction transaction) {
    return mapper.toDomain(repository.save(mapper.toEntity(transaction)));
  }

  /**
   * Finds a transaction by id.
   *
   * @param id transaction identifier
   * @return matching transaction, or empty when absent
   */
  @Override
  public Optional<Transaction> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  /**
   * Lists all transactions.
   *
   * @return transaction aggregates
   */
  @Override
  public List<Transaction> findAll() {
    return repository.findAll()
      .stream()
      .map(mapper::toDomain)
      .toList();
  }

  /**
   * Lists transactions where the account appears as source or destination.
   *
   * @param accountId account identifier
   * @return matching transaction aggregates
   */
  @Override
  public List<Transaction> findByAccountId(UUID accountId) {
    return repository.findByAccount_IdOrDestinationAccount_Id(accountId, accountId)
      .stream()
      .map(mapper::toDomain)
      .toList();
  }

  /**
   * Checks whether a transaction exists for the supplied id.
   *
   * @param id transaction identifier
   * @return true when a transaction exists
   */
  @Override
  public boolean existsById(UUID id) {
    return repository.existsById(id);
  }

  /**
   * Checks whether a cancellation transaction references the supplied original
   * transaction.
   *
   * @param cancelledTransactionId original transaction identifier
   * @return true when the original transaction has already been cancelled
   */
  @Override
  public boolean existsByCancelledTransactionId(UUID cancelledTransactionId) {
    return repository.existsByCancelledTransaction_Id(cancelledTransactionId);
  }
}
