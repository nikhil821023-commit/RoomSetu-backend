ALTER TABLE users ADD COLUMN work_email VARCHAR(150);
ALTER TABLE users ADD COLUMN work_email_verified_at TIMESTAMP;

-- One verified work email can belong to only one account.
CREATE UNIQUE INDEX uq_users_work_email ON users (LOWER(work_email)) WHERE work_email IS NOT NULL;