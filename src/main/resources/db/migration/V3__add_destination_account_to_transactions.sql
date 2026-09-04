ALTER TABLE transactions
ADD COLUMN destination_account_id UUID;

ALTER TABLE transactions
ADD CONSTRAINT fk_transactions_destination_account
FOREIGN KEY (destination_account_id) REFERENCES accounts(id);

CREATE INDEX IF NOT EXISTS idx_transactions_destination_account_id
ON transactions(destination_account_id);
