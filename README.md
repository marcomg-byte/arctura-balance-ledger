# Payment Bridge

Payment Bridge is a minimal Spring Boot RESTful API for handling financial
transactions. The project follows a lightweight Domain-Driven Design approach
so the business model stays separated from HTTP, persistence, and framework
details.

Although the project name includes "bridge", the application is intended to be
a simple transaction API, not an integration bridge between payment providers.

## Architecture

The application is organized around DDD layers:

- `domain`: core business concepts and rules.
- `application`: use cases and orchestration.
- `infrastructure`: technical details such as persistence and ORM mappings.
- `interfaces`: external entry points such as REST controllers.

The chosen package structure is:

```text
src/main/java/com/arctura/payment_bridge
├── PaymentBridgeApplication.java
│
├── domain
│   ├── account
│   │   ├── Account.java
│   │   ├── AccountRepository.java
│   │   └── Balance.java
│   │
│   ├── shared
│   │   ├── Currency.java
│   │   └── Money.java
│   │
│   └── transaction
│       ├── Transaction.java
│       ├── TransactionRepository.java
│       └── TransactionType.java
│
├── application
│   └── TransactionService.java
│
├── infrastructure
│   └── persistence
│       └── jpa
│           ├── entities
│           │   ├── AccountEntity.java
│           │   └── TransactionEntity.java
│           │
│           ├── repositories
│           │   ├── JpaAccountRepository.java
│           │   └── JpaTransactionRepository.java
│           │
│           └── mappers
│               ├── AccountMapper.java
│               └── TransactionMapper.java
│
└── interfaces
    └── rest
        ├── AccountController.java
        ├── TransactionController.java
        ├── CreateTransactionRequest.java
        ├── UpdateTransactionRequest.java
        └── TransactionResponse.java
```

Some of these packages represent the intended structure and may be added as the
API grows.

## Domain Model

### Account

`Account` represents the owner of a balance. It is a domain entity, so it has an
identity and owns business behavior related to balance changes.

Balance changes should be expressed through domain methods such as:

```java
account.increaseBalance(amount);
account.decreaseBalance(amount);
```

This avoids replacing the balance directly from outside the entity.

### Balance

`Balance` represents the current monetary state of an account. It wraps a
`Money` value and exposes operations such as increasing or decreasing the
current amount.

### Money

`Money` is a shared domain value object. It combines:

- an amount, represented with `BigDecimal`
- a currency, represented by `Currency`

`BigDecimal` is used instead of `double` because financial calculations require
decimal precision.

Example:

```java
Money amount = new Money(new BigDecimal("100.00"), Currency.MXN);
```

### Currency

`Currency` represents the monetary unit used by a `Money` value, such as `USD`,
`MXN`, or `EUR`.

### Transaction

`Transaction` represents a financial movement associated with an account. A
minimal transaction includes:

- an id
- an account id
- a transaction type
- a money amount
- an optional description
- a creation timestamp

### TransactionType

`TransactionType` describes the kind of transaction.

Current examples:

```java
INCOME
EXPENSE
TRANSFER
```

## Persistence

Domain classes should not contain JPA or Hibernate annotations.

ORM-specific classes live under:

```text
infrastructure/persistence/jpa
```

The distinction is:

- `domain/.../Account.java`: business entity.
- `infrastructure/persistence/jpa/entities/AccountEntity.java`: ORM entity.
- `domain/.../Transaction.java`: business entity.
- `infrastructure/persistence/jpa/entities/TransactionEntity.java`: ORM entity.

Mappers convert between domain objects and persistence entities.

## REST API

The intended minimal REST API is:

```text
GET    /transactions
GET    /transactions/{id}
POST   /transactions
PUT    /transactions/{id}
DELETE /transactions/{id}
```

Account endpoints may be added if account data becomes part of the public API.

## Development

Run the application:

```bash
./gradlew bootRun
```

Run tests:

```bash
./gradlew test
```
