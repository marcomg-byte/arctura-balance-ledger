CREATE TABLE IF NOT EXISTS accounts (
  id VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  paternal_surname VARCHAR(255) NOT NULL,
  maternal_surname VARCHAR(255) NOT NULL,
  balance_amount NUMERIC(19, 2) NOT NULL,
  balance_currency VARCHAR(255) NOT NULL,
  CONSTRAINT pk_accounts PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS transactions (
  id VARCHAR(255) NOT NULL,
  account_id VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  amount NUMERIC(19, 2) NOT NULL,
  currency VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT pk_transactions PRIMARY KEY (id),
  CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
