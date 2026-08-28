package com.arctura.payment_bridge.domain.transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
  Transaction save(Transaction transaction);
  Optional<Transaction> findById(String id);
  List<Transaction> findAll();
  List<Transaction> findByAccountId(String accountId);
  boolean existsById(String id);
  void deleteById(String id);
}
