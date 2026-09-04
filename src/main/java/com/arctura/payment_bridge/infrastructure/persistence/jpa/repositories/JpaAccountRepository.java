package com.arctura.payment_bridge.infrastructure.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arctura.payment_bridge.domain.account.Account;
import com.arctura.payment_bridge.domain.account.AccountRepository;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.entities.AccountEntity;
import com.arctura.payment_bridge.infrastructure.persistence.jpa.mappers.AccountMapper;

interface SpringDataAccountRepository extends JpaRepository<AccountEntity, UUID> {
  Optional<AccountEntity> findByIdAndDeletedAtIsNull(UUID id);
  List<AccountEntity> findByDeletedAtIsNull();
  List<AccountEntity> findByNameAndDeletedAtIsNull(String name);
  boolean existsByIdAndDeletedAtIsNull(UUID id);
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
  public Optional<Account> findById(UUID id) {
    return repository.findByIdAndDeletedAtIsNull(id).map(this.mapper::toDomain);
  }

  @Override
  public List<Account> findAll() {
    return repository.findByDeletedAtIsNull()
      .stream()
      .map(this.mapper::toDomain)
      .toList();
  }

  @Override
  public boolean existsById(UUID id) {
    return repository.existsByIdAndDeletedAtIsNull(id);
  }

  @Override
  public void deleteById(UUID id) {
    repository.findById(id).ifPresent(account -> {
      account.setDeletedAt(java.time.LocalDateTime.now());
      repository.save(account);
    });
  }

  @Override
  public List<Account> findByName(String name) {
    return repository.findByNameAndDeletedAtIsNull(name)
      .stream()
      .map(this.mapper::toDomain)
      .toList();
  }
}
