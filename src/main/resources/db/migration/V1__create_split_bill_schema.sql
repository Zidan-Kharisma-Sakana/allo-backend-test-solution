CREATE TABLE trips (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    owner_session_id VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE participants (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips(id),
    name VARCHAR(100) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL REFERENCES trips(id),
    payer_id UUID NOT NULL REFERENCES participants(id),
    amount NUMERIC NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE expense_shares (
    expense_id UUID NOT NULL REFERENCES expenses(id),
    participant_id UUID NOT NULL REFERENCES participants(id),
    amount NUMERIC NOT NULL,
    PRIMARY KEY (expense_id, participant_id)
);
