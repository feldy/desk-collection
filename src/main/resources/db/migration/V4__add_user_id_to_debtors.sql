ALTER TABLE debtors ADD COLUMN user_id BIGINT;

ALTER TABLE debtors 
    ADD CONSTRAINT fk_debtors_users 
    FOREIGN KEY (user_id) 
    REFERENCES users (id);
