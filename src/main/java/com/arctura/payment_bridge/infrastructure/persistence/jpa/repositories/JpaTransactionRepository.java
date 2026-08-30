package com.arctura.payment_bridge.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arctura.payment_bridge.domain.shared.Money;
import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionRepository;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.TransactionEntity;

interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, String> {
  List<TransactionEntity> findByAccountId(String accountId);
}

@Repository
public class JpaTransactionRepository implements TransactionRepository {
  private final SpringDataTransactionRepository repository;

  public JpaTransactionRepository(SpringDataTransactionRepository repository) {
    this.repository = repository;
  }

  @Override
  public Transaction save(Transaction transaction) {
    return toDomain(repository.save(toEntity(transaction)));
  }

  @Override
  public Optional<Transaction> findById(String id) {
    return repository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Transaction> findAll() {
    return repository.findAll()
      .stream()
      .map(this::toDomain)
      .toList();
  }

  @Override
  public List<Transaction> findByAccountId(String accountId) {
    return repository.findByAccountId(accountId)
      .stream()
      .map(this::toDomain)
      .toList();
  }

  @Override
  public boolean existsById(String id) {
    return repository.existsById(id);
  }

  @Override
  public void deleteById(String id) {
    repository.deleteById(id);
  }

  private TransactionEntity toEntity(Transaction transaction) {
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

  private Transaction toDomain(TransactionEntity entity) {
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
