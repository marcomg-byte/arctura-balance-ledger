UPDATE transactions
SET destination_account_id = NULL
WHERE type <> 'TRANSFER'
  AND destination_account_id IS NOT NULL;

ALTER TABLE transactions
ADD CONSTRAINT chk_transactions_destination_account_rules
CHECK (
  (
    type = 'TRANSFER'
    AND destination_account_id IS NOT NULL
    AND account_id <> destination_account_id
  )
  OR (
    type <> 'TRANSFER'
    AND destination_account_id IS NULL
  )
);
