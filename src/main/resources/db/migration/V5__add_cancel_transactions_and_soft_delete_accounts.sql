ALTER TABLE accounts
ADD COLUMN deleted_at TIMESTAMP(6);

ALTER TABLE transactions
ADD COLUMN cancelled_transaction_id UUID;

ALTER TABLE transactions
DROP CONSTRAINT chk_transactions_destination_account_rules;

ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_cancelled_transaction
FOREIGN KEY (cancelled_transaction_id) REFERENCES transactions(id);

ALTER TABLE transactions
ADD CONSTRAINT uq_transactions_cancelled_transaction
UNIQUE (cancelled_transaction_id);

CREATE INDEX IF NOT EXISTS idx_transactions_cancelled_transaction_id
ON transactions(cancelled_transaction_id);

ALTER TABLE transactions
ADD CONSTRAINT chk_transactions_cancellation_rules
CHECK (
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
);
