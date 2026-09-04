package com.arctura.balance_ledger.domain.transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository port for transactions.
 *
 * <p>Application services use this abstraction to persist ledger entries and
 * query cancellation relationships without depending on JPA or database query
 * details.</p>
 */
public interface TransactionRepository {
  /**
   * Persists a transaction aggregate.
   *
   * @param transaction aggregate to save
   * @return saved aggregate as reloaded or returned by persistence
   */
  Transaction save(Transaction transaction);

  /**
   * Finds a transaction by id.
   *
   * @param id transaction identifier
   * @return matching transaction, or empty when not found
   */
  Optional<Transaction> findById(UUID id);

  /**
   * Lists all transactions.
   *
   * @return transaction aggregates
   */
  List<Transaction> findAll();

  /**
   * Lists transactions where the account participates as source or destination.
   *
   * @param accountId account identifier
   * @return matching transaction aggregates
   */
  List<Transaction> findByAccountId(UUID accountId);

  /**
   * Checks whether a transaction exists.
   *
   * @param id transaction identifier
   * @return true when a transaction exists for the id
   */
  boolean existsById(UUID id);

  /**
   * Checks whether a transaction already has a cancellation record.
   *
   * @param cancelledTransactionId identifier of the transaction being cancelled
   * @return true when any cancellation references the supplied transaction id
   */
  boolean existsByCancelledTransactionId(UUID cancelledTransactionId);
}
