package com.arctura.payment_bridge.domain.account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
  Account save(Account account);
  Optional<Account> findById(UUID id);
  List<Account> findAll();
  boolean existsById(UUID id);
  void deleteById(UUID id);
  List<Account> findByName(String name);
}
