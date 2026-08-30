package com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers;

import org.springframework.stereotype.Component;

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.TransactionEntity;

@Component
public class TransactionMapper {
  public TransactionEntity toEntity(Transaction transaction) {
    Money amount = transaction.getAmount();

    return new TransactionEntity(
      transaction.getId(),
      transaction.getAccountId(),
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
