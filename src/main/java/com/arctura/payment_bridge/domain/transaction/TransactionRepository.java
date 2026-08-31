package com.arctura.payment_bridge.domain.transaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
  Transaction save(Transaction transaction);
  Optional<Transaction> findById(UUID id);
  List<Transaction> findAll();
  List<Transaction> findByAccountId(UUID accountId);
  boolean existsById(UUID id);
  void deleteById(UUID id);
}
