CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE transactions DROP CONSTRAINT fk_transactions_account;
ALTER TABLE transactions DROP CONSTRAINT pk_transactions;
ALTER TABLE accounts DROP CONSTRAINT pk_accounts;

ALTER TABLE accounts ADD COLUMN uuid_id UUID;
UPDATE accounts SET uuid_id = gen_random_uuid() WHERE uuid_id IS NULL;
ALTER TABLE accounts ALTER COLUMN uuid_id SET NOT NULL;

ALTER TABLE transactions ADD COLUMN uuid_id UUID;
ALTER TABLE transactions ADD COLUMN uuid_account_id UUID;
UPDATE transactions SET uuid_id = gen_random_uuid() WHERE uuid_id IS NULL;
UPDATE transactions
SET uuid_account_id = accounts.uuid_id
FROM accounts
WHERE transactions.account_id = accounts.id;
ALTER TABLE transactions ALTER COLUMN uuid_id SET NOT NULL;
ALTER TABLE transactions ALTER COLUMN uuid_account_id SET NOT NULL;

ALTER TABLE transactions DROP COLUMN account_id;
ALTER TABLE transactions DROP COLUMN id;
ALTER TABLE accounts DROP COLUMN id;

ALTER TABLE accounts RENAME COLUMN uuid_id TO id;
ALTER TABLE transactions RENAME COLUMN uuid_id TO id;
ALTER TABLE transactions RENAME COLUMN uuid_account_id TO account_id;

ALTER TABLE accounts ADD CONSTRAINT pk_accounts PRIMARY KEY (id);
ALTER TABLE transactions ADD CONSTRAINT pk_transactions PRIMARY KEY (id);
ALTER TABLE transactions
  ADD CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
