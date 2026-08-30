package com.arctura.payment_bridge.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.AccountEntity;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers.AccountMapper;

interface SpringDataAccountRepository extends JpaRepository<AccountEntity, String> {
  List<AccountEntity> findByName(String name);
}

@Repository
public class JpaAccountRepository implements AccountRepository {
  private final SpringDataAccountRepository repository;
  private final AccountMapper mapper;

  public JpaAccountRepository(SpringDataAccountRepository repository, AccountMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Account save(Account account) {
    return this.mapper.toDomain(repository.save(this.mapper.toEntity(account)));
  }

  @Override
  public Optional<Account> findById(String id) {
    return repository.findById(id).map(this.mapper::toDomain);
  }

  @Override
  public List<Account> findAll() {
    return repository.findAll()
      .stream()
      .map(this.mapper::toDomain)
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

  @Override
  public List<Account> findByName(String name) {
    return repository.findByName(name)
      .stream()
      .map(this.mapper::toDomain)
      .toList();
  }
}
