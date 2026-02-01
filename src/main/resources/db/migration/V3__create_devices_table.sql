CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    session_name VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50),
    user_id BIGINT UNIQUE REFERENCES users(id),
    created_at TIMESTAMP
);
