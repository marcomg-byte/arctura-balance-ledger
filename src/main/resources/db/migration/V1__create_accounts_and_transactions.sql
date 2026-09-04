CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS accounts (
  id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  paternal_surname VARCHAR(255) NOT NULL,
  maternal_surname VARCHAR(255) NOT NULL,
  balance_amount NUMERIC(19, 2) NOT NULL,
  balance_currency VARCHAR(255) NOT NULL,
  deleted_at TIMESTAMP(6),
  CONSTRAINT pk_accounts PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS transactions (
  id UUID NOT NULL,
  account_id UUID NOT NULL,
  destination_account_id UUID,
  cancelled_transaction_id UUID,
  type VARCHAR(255) NOT NULL,
  amount NUMERIC(19, 2) NOT NULL,
  currency VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT pk_transactions PRIMARY KEY (id),
  CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id),
  CONSTRAINT fk_transactions_destination_account FOREIGN KEY (destination_account_id) REFERENCES accounts(id),
  CONSTRAINT fk_transactions_cancelled_transaction FOREIGN KEY (cancelled_transaction_id) REFERENCES transactions(id),
  CONSTRAINT uq_transactions_cancelled_transaction UNIQUE (cancelled_transaction_id),
  CONSTRAINT chk_transactions_cancellation_rules CHECK (
    (
      type = 'TRANSFER'
      AND destination_account_id IS NOT NULL
      AND account_id <> destination_account_id
      AND cancelled_transaction_id IS NULL
    )
    OR (
      type = 'CANCEL'
      AND cancelled_transaction_id IS NOT NULL
      AND (
        destination_account_id IS NULL
        OR account_id <> destination_account_id
      )
    )
    OR (
      type NOT IN ('TRANSFER', 'CANCEL')
      AND destination_account_id IS NULL
      AND cancelled_transaction_id IS NULL
    )
  )
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_destination_account_id ON transactions(destination_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_cancelled_transaction_id ON transactions(cancelled_transaction_id);
