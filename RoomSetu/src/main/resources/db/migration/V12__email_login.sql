ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE users ADD COLUMN email VARCHAR(150);

CREATE UNIQUE INDEX uq_users_login_email ON users (email) WHERE email IS NOT NULL;

-- Every account needs at least one way to sign in.
ALTER TABLE users ADD CONSTRAINT chk_users_has_identifier CHECK (phone IS NOT NULL OR email IS NOT NULL);