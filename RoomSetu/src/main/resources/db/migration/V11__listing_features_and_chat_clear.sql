ALTER TABLE listings ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE listing_features (
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    feature    VARCHAR(40) NOT NULL,
    PRIMARY KEY (listing_id, feature)
);

ALTER TABLE conversations ADD COLUMN user_a_cleared_at TIMESTAMP;
ALTER TABLE conversations ADD COLUMN user_b_cleared_at TIMESTAMP;