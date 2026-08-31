package com.arctura.payment_bridge.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arctura.payment_bridge.domain.transaction.Transaction;
import com.arctura.payment_bridge.domain.transaction.TransactionRepository;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.TransactionEntity;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers.TransactionMapper;

interface SpringDataTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
  List<TransactionEntity> findByAccount_Id(UUID accountId);
}

@Repository
public class JpaTransactionRepository implements TransactionRepository {
  private final SpringDataTransactionRepository repository;
  private final TransactionMapper mapper;

  public JpaTransactionRepository(
    SpringDataTransactionRepository repository,
    TransactionMapper mapper
  ) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Transaction save(Transaction transaction) {
    return mapper.toDomain(repository.save(mapper.toEntity(transaction)));
  }

  @Override
  public Optional<Transaction> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Transaction> findAll() {
    return repository.findAll()
      .stream()
      .map(mapper::toDomain)
      .toList();
  }

  @Override
  public List<Transaction> findByAccountId(UUID accountId) {
    return repository.findByAccount_Id(accountId)
      .stream()
      .map(mapper::toDomain)
      .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return repository.existsById(id);
  }

  @Override
  public void deleteById(UUID id) {
    repository.deleteById(id);
  }
}
