ALTER TABLE bill_groups ADD COLUMN invite_code VARCHAR(6);
ALTER TABLE bill_groups ALTER COLUMN invite_code SET NOT NULL;
ALTER TABLE bill_groups ADD CONSTRAINT bill_groups_invite_code_unique UNIQUE (invite_code);
ALTER TABLE bill_groups ADD CONSTRAINT bill_groups_invite_code_format CHECK (invite_code ~ '^[A-Z0-9]{6}$');
