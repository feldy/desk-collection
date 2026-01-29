CREATE TABLE desks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    dimensions VARCHAR(255),
    material VARCHAR(255),
    price DOUBLE PRECISION,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
