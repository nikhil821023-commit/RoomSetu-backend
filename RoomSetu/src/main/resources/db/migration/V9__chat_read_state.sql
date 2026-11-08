ALTER TABLE conversations ADD COLUMN user_a_last_read_at TIMESTAMP;
ALTER TABLE conversations ADD COLUMN user_b_last_read_at TIMESTAMP;

-- Treat existing history as already read, so nobody opens the app to a pile of "unread".
UPDATE conversations SET user_a_last_read_at = NOW(), user_b_last_read_at = NOW();