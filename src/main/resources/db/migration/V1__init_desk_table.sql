DROP TABLE IF EXISTS desks;

CREATE TABLE debtors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    debtor_id BIGINT NOT NULL REFERENCES debtors(id) ON DELETE CASCADE,
    amount NUMERIC(19, 2) NOT NULL,
    remaining_amount NUMERIC(19, 2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50)
);

CREATE TABLE interactions (
    id BIGSERIAL PRIMARY KEY,
    debtor_id BIGINT NOT NULL REFERENCES debtors(id) ON DELETE CASCADE,
    type VARCHAR(50),
    notes TEXT,
    interaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    outcome VARCHAR(255)
);
