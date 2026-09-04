package com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.AccountEntity;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.TransactionEntity;

import jakarta.persistence.EntityManager;

@Component
public class TransactionMapper {
  private final EntityManager entityManager;

  public TransactionMapper(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

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
