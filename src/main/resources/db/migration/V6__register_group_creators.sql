INSERT INTO group_members (bill_group_id, session_id)
SELECT id, owner_session_id FROM bill_groups
ON CONFLICT DO NOTHING;
