CREATE TABLE group_members (
    bill_group_id UUID NOT NULL REFERENCES bill_groups(id),
    session_id VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (bill_group_id, session_id)
);
CREATE INDEX group_members_session_idx ON group_members(session_id);
