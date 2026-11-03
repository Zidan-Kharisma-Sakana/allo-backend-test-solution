ALTER TABLE bill_groups ADD COLUMN invite_code VARCHAR(6);
ALTER TABLE bill_groups ADD CONSTRAINT bill_groups_invite_code_unique UNIQUE (invite_code);
DO $$
DECLARE group_id UUID;
BEGIN
    FOR group_id IN SELECT id FROM bill_groups LOOP
        LOOP
            BEGIN
                UPDATE bill_groups SET invite_code = upper(substr(md5(random()::text || group_id::text), 1, 6)) WHERE id = group_id;
                EXIT;
            EXCEPTION WHEN unique_violation THEN
                NULL;
            END;
        END LOOP;
    END LOOP;
END $$;
ALTER TABLE bill_groups ALTER COLUMN invite_code SET NOT NULL;
ALTER TABLE bill_groups ADD CONSTRAINT bill_groups_invite_code_format CHECK (invite_code ~ '^[A-Z0-9]{6}$');
