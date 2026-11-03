ALTER TABLE expenses ADD COLUMN idempotency_key VARCHAR(128);
ALTER TABLE expenses ADD COLUMN request_fingerprint VARCHAR(64);
ALTER TABLE expenses ADD CONSTRAINT expenses_group_request_key_unique UNIQUE (bill_group_id, idempotency_key);
