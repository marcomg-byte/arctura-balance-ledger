package com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.AccountEntity;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.TransactionEntity;

import jakarta.persistence.EntityManager;

/**
 * Converts transactions between domain aggregates and JPA entities while using
 * references for related persisted records.
 *
 * <p>Relationship fields are resolved through {@link EntityManager#getReference}
 * to avoid unnecessary database fetches when persisting a transaction that only
 * needs foreign-key references.</p>
 */
@Component
public class TransactionMapper {
  private final EntityManager entityManager;

  /**
   * Creates the mapper with the entity manager used to create relationship
   * references.
   *
   * @param entityManager JPA entity manager
   */
  public TransactionMapper(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Converts a domain transaction into a JPA entity ready for persistence.
   *
   * @param transaction domain aggregate to convert
   * @return transaction entity containing persisted values and relationships
   */
  public TransactionEntity toEntity(Transaction transaction) {
    Money amount = transaction.getAmount();
    AccountEntity account = entityManager.getReference(AccountEntity.class, transaction.getAccountId());
    AccountEntity destinationAccount = transaction.getDestinationAccountId() == null
      ? null
      : entityManager.getReference(AccountEntity.class, transaction.getDestinationAccountId());
    TransactionEntity cancelledTransaction = transaction.getCancelledTransactionId() == null
      ? null
      : entityManager.getReference(TransactionEntity.class, transaction.getCancelledTransactionId());

    return new TransactionEntity(
      transaction.getId(),
      account,
      destinationAccount,
      cancelledTransaction,
      transaction.getType(),
      amount.getAmount(),
      amount.getCurrency(),
      transaction.getDescription(),
      transaction.getCreatedAt()
    );
  }

  /**
   * Converts a JPA transaction entity into a domain aggregate.
   *
   * @param entity persisted transaction entity to convert
   * @return domain transaction aggregate
   */
  public Transaction toDomain(TransactionEntity entity) {
    Money amount = new Money(entity.getAmount(), entity.getCurrency());

    return new Transaction(
      entity.getId(),
      entity.getAccountId(),
      entity.getDestinationAccountId(),
      entity.getCancelledTransactionId(),
      entity.getType(),
      amount,
      entity.getDescription(),
      entity.getCreatedAt()
    );
  }
}
