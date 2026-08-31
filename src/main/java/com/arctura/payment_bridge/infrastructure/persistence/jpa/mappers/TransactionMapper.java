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

    return new TransactionEntity(
      transaction.getId(),
      account,
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
      entity.getType(),
      amount,
      entity.getDescription(),
      entity.getCreatedAt()
    );
  }
}
