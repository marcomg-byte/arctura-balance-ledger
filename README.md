# Payment Bridge

Payment Bridge is a Spring Boot REST API for managing accounts and financial
transactions. The project follows a lightweight Domain-Driven Design approach:
domain models stay free of framework annotations, while HTTP and persistence
concerns live in their own infrastructure/interface layers.

Although the project name includes "bridge", the current application is a
simple account and transaction API, not an integration bridge between payment
providers.

## Current Stack

- Java 26
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Gradle

## Architecture

The application is organized around these layers:

- `domain`: business entities, value objects, enums, and repository contracts.
- `infrastructure`: technical implementations, currently JPA persistence.
- `interfaces`: external entry points, currently REST controllers and DTOs.

There is no separate `application` layer yet. Controllers currently call domain
repository interfaces directly. As the project grows, use cases/services can be
introduced between the REST layer and the domain repositories.

## Project Structure

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
├── infrastructure
│   └── persistence
│       └── jpa
│           ├── entities
│           │   ├── AccountEntity.java
│           │   └── TransactionEntity.java
│           │
│           ├── mappers
│           │   ├── AccountMapper.java
│           │   └── TransactionMapper.java
│           │
│           └── repositories
│               ├── JpaAccountRepository.java
│               └── JpaTransactionRepository.java
│
└── interfaces
    └── rest
        ├── accounts
        │   ├── controllers
        │   │   └── AccountController.java
        │   ├── requests
        │   │   ├── CreateAccountRequest.java
        │   │   └── UpdatePersonalInfoRequest.java
        │   └── responses
        │       └── AccountResponse.java
        │
        └── transactions
            ├── controllers
            │   └── TransactionController.java
            ├── requests
            │   ├── CreateTransactionRequest.java
            │   └── UpdateTransactionRequest.java
            └── responses
                └── TransactionResponse.java
```

`PaymentBridgeApplication` is located in the root package
`com.arctura.payment_bridge`, so Spring Boot automatically scans the domain,
infrastructure, and interfaces subpackages. No custom `@ComponentScan` is
currently required.

## Domain Model

### Account

`Account` represents the owner of a balance. It has an identity, personal
information, and domain behavior for changing the balance.

Balance changes are expressed through domain methods:

```java
account.increaseBalance(amount);
account.decreaseBalance(amount);
```

### Balance

`Balance` wraps a `Money` value and represents the current monetary state of an
account.

### Money

`Money` is a shared value object made of:

- `BigDecimal amount`
- `Currency currency`

`BigDecimal` is used for financial precision.

Example:

```java
Money amount = new Money(new BigDecimal("100.00"), Currency.MXN);
```

### Currency

`Currency` represents the monetary unit used by a `Money` value. Current values
are `USD`, `MXN`, and `EUR`.

### Transaction

`Transaction` represents a financial movement associated with an account. It
contains:

- an id
- an account id
- a transaction type
- a money amount
- a description
- a creation timestamp

New transactions use the constructor that assigns `createdAt` automatically.
Persisted transactions can be rehydrated through the constructor that accepts
the original `createdAt` value.

### TransactionType

Current transaction types are:

```java
INCOME
EXPENSE
TRANSFER
```

## Persistence

Domain classes do not contain JPA annotations. Persistence-specific classes
live under:

```text
infrastructure/persistence/jpa
```

The persistence layer contains:

- JPA entities for database mapping.
- Mapper classes for converting between domain objects and JPA entities.
- Repository adapters that implement the domain repository interfaces using
  Spring Data JPA.

For example:

- `domain/account/Account.java` is the business entity.
- `infrastructure/persistence/jpa/entities/AccountEntity.java` is the JPA
  entity.
- `AccountMapper` converts between both representations.
- `JpaAccountRepository` implements the domain `AccountRepository`.

## REST API

### Accounts

```text
POST   /accounts
GET    /accounts
GET    /accounts?name={name}
GET    /accounts/{id}
PATCH  /accounts/{id}/personal-info
DELETE /accounts/{id}
```

`POST /accounts` accepts:

```json
{
  "id": "acc-1",
  "name": "Marco",
  "paternalSurname": "Doe",
  "maternalSurname": "Smith",
  "balanceAmount": 1000.00,
  "balanceCurrency": "MXN"
}
```

`PATCH /accounts/{id}/personal-info` accepts:

```json
{
  "name": "Marco",
  "paternalSurname": "Doe",
  "maternalSurname": "Smith"
}
```

### Transactions

```text
POST   /transactions
GET    /transactions
GET    /transactions?accountId={accountId}
GET    /transactions/{id}
PATCH  /transactions/{id}
DELETE /transactions/{id}
```

`POST /transactions` accepts:

```json
{
  "id": "txn-1",
  "accountId": "acc-1",
  "type": "INCOME",
  "amount": 250.00,
  "currency": "MXN",
  "description": "Initial deposit"
}
```

`PATCH /transactions/{id}` accepts:

```json
{
  "type": "EXPENSE",
  "amount": 50.00,
  "currency": "MXN",
  "description": "Updated description"
}
```

## Configuration

The application currently defines only:

```properties
spring.application.name=payment_bridge
```

Because Spring Data JPA is enabled, running the full application or context
tests will require datasource configuration, such as an embedded database for
local development or a real database connection.

## Development

Compile the project:

```bash
./gradlew compileJava
```

Run tests:

```bash
./gradlew test
```

Run the application:

```bash
./gradlew bootRun
```
