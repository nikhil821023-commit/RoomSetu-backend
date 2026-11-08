CREATE TABLE conversations (
    id           BIGSERIAL PRIMARY KEY,
    user_a_id    BIGINT NOT NULL REFERENCES users(id),
    user_b_id    BIGINT NOT NULL REFERENCES users(id),
    created_at   TIMESTAMP NOT NULL,
    UNIQUE (user_a_id, user_b_id)
);

CREATE TABLE messages (
    id                BIGSERIAL PRIMARY KEY,
    conversation_id   BIGINT NOT NULL REFERENCES conversations(id),
    sender_id         BIGINT NOT NULL REFERENCES users(id),
    content           VARCHAR(2000) NOT NULL,
    sent_at           TIMESTAMP NOT NULL
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, sent_at);