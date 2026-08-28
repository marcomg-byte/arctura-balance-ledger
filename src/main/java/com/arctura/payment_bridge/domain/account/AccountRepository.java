package com.arctura.payment_bridge.domain.account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
  Account save(Account account);
  Optional<Account> findById(String id);
  List<Account> findAll();
  boolean existsById(String id);
  void deleteById(String id);
  List<Account> findByName(String name);
}
