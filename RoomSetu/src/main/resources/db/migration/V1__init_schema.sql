-- Consolidated baseline covering Phases 1-6 plus the admin flag added in this hardening pass.
-- This is the schema exactly as the entities define it as of today — going forward,
-- every future schema change gets its own V2, V3, ... file. Never edit this file once
-- it has run against any real database; Flyway checksums it.

CREATE TABLE users (
    id                    BIGSERIAL PRIMARY KEY,
    phone                 VARCHAR(15) NOT NULL UNIQUE,
    name                  VARCHAR(100) NOT NULL,
    role                  VARCHAR(20) NOT NULL,
    verification_status   VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    admin                 BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP NOT NULL
);

CREATE TABLE profiles (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT NOT NULL UNIQUE REFERENCES users(id),
    budget_min         INT NOT NULL,
    budget_max         INT NOT NULL,
    locality           VARCHAR(100) NOT NULL,
    sleep_schedule     VARCHAR(20),
    work_from_home     VARCHAR(20),
    food_preference    VARCHAR(20),
    cleanliness        VARCHAR(20),
    smokes             BOOLEAN,
    drinks             BOOLEAN,
    has_pets           BOOLEAN,
    ok_with_pets       BOOLEAN,
    bio                VARCHAR(500)
);

CREATE TABLE listings (
    id                    BIGSERIAL PRIMARY KEY,
    host_id               BIGINT NOT NULL REFERENCES users(id),
    locality              VARCHAR(150) NOT NULL,
    address               VARCHAR(300) NOT NULL,
    rent                  INT NOT NULL,
    lease_type            VARCHAR(20) NOT NULL,
    available_from        DATE NOT NULL,
    verification_status   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    filled                BOOLEAN NOT NULL DEFAULT FALSE,
    description           VARCHAR(1000),
    created_at            TIMESTAMP NOT NULL
);

CREATE TABLE listing_photos (
    listing_id   BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    photo_url    VARCHAR(255)
);

CREATE TABLE verifications (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id),
    doc_type          VARCHAR(40) NOT NULL,
    doc_url           VARCHAR(500) NOT NULL,
    review_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by       VARCHAR(255),
    reviewed_at       TIMESTAMP,
    submitted_at      TIMESTAMP NOT NULL
);

CREATE TABLE matches (
    id                     BIGSERIAL PRIMARY KEY,
    requester_id           BIGINT NOT NULL REFERENCES users(id),
    target_id              BIGINT NOT NULL REFERENCES users(id),
    compatibility_score    INT NOT NULL,
    status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at             TIMESTAMP NOT NULL,
    responded_at           TIMESTAMP,
    UNIQUE (requester_id, target_id)
);

CREATE TABLE reviews (
    id                BIGSERIAL PRIMARY KEY,
    reviewer_id       BIGINT NOT NULL REFERENCES users(id),
    reviewee_id       BIGINT NOT NULL REFERENCES users(id),
    match_id          BIGINT REFERENCES matches(id),
    rating            INT NOT NULL,
    comment           VARCHAR(500),
    tenancy_period    VARCHAR(100),
    created_at        TIMESTAMP NOT NULL,
    UNIQUE (reviewer_id, reviewee_id, match_id)
);

CREATE TABLE fraud_reports (
    id             BIGSERIAL PRIMARY KEY,
    reporter_id    BIGINT NOT NULL REFERENCES users(id),
    target_type    VARCHAR(20) NOT NULL,
    target_id      BIGINT NOT NULL,
    reason         VARCHAR(500) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reviewed_by    VARCHAR(255),
    reviewed_at    TIMESTAMP,
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE blocks (
    id            BIGSERIAL PRIMARY KEY,
    blocker_id    BIGINT NOT NULL REFERENCES users(id),
    blocked_id    BIGINT NOT NULL REFERENCES users(id),
    created_at    TIMESTAMP NOT NULL,
    UNIQUE (blocker_id, blocked_id)
);

CREATE TABLE listing_verifications (
    id                BIGSERIAL PRIMARY KEY,
    listing_id        BIGINT NOT NULL REFERENCES listings(id),
    doc_type          VARCHAR(40) NOT NULL,
    doc_url           VARCHAR(500) NOT NULL,
    review_status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by       VARCHAR(255),
    reviewed_at       TIMESTAMP,
    submitted_at      TIMESTAMP NOT NULL
);

CREATE TABLE checklist_items (
    id              BIGSERIAL PRIMARY KEY,
    match_id        BIGINT NOT NULL REFERENCES matches(id),
    title           VARCHAR(150) NOT NULL,
    note            VARCHAR(300),
    done            BOOLEAN NOT NULL DEFAULT FALSE,
    created_by      BIGINT NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL,
    completed_at    TIMESTAMP
);

CREATE TABLE expenses (
    id             BIGSERIAL PRIMARY KEY,
    match_id       BIGINT NOT NULL REFERENCES matches(id),
    description    VARCHAR(150) NOT NULL,
    amount         INT NOT NULL,
    paid_by        BIGINT NOT NULL REFERENCES users(id),
    category       VARCHAR(20) NOT NULL,
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE reminders (
    id                   BIGSERIAL PRIMARY KEY,
    match_id             BIGINT NOT NULL REFERENCES matches(id),
    type                 VARCHAR(20) NOT NULL,
    title                VARCHAR(150) NOT NULL,
    due_date             DATE NOT NULL,
    recurring_monthly    BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL
);

CREATE TABLE shopping_list_items (
    id            BIGSERIAL PRIMARY KEY,
    match_id      BIGINT NOT NULL REFERENCES matches(id),
    item          VARCHAR(100) NOT NULL,
    added_by      BIGINT NOT NULL REFERENCES users(id),
    purchased     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP NOT NULL
);

CREATE TABLE listing_views (
    id            BIGSERIAL PRIMARY KEY,
    listing_id    BIGINT NOT NULL REFERENCES listings(id),
    viewer_id     BIGINT REFERENCES users(id),
    viewed_at     TIMESTAMP NOT NULL
);

CREATE TABLE listing_inquiries (
    id            BIGSERIAL PRIMARY KEY,
    listing_id    BIGINT NOT NULL REFERENCES listings(id),
    inquirer_id   BIGINT NOT NULL REFERENCES users(id),
    message       VARCHAR(500),
    created_at    TIMESTAMP NOT NULL
);

CREATE TABLE referral_codes (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(12) NOT NULL UNIQUE,
    owner_id      BIGINT NOT NULL UNIQUE REFERENCES users(id),
    created_at    TIMESTAMP NOT NULL
);

CREATE TABLE referrals (
    id                   BIGSERIAL PRIMARY KEY,
    referral_code_id     BIGINT NOT NULL REFERENCES referral_codes(id),
    referred_user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id),
    status               VARCHAR(20) NOT NULL DEFAULT 'SIGNED_UP',
    created_at           TIMESTAMP NOT NULL,
    rewarded_at          TIMESTAMP
);

CREATE TABLE partner_organizations (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    type           VARCHAR(20) NOT NULL,
    signup_code    VARCHAR(20) NOT NULL UNIQUE,
    locality       VARCHAR(100) NOT NULL,
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE partner_memberships (
    id                BIGSERIAL PRIMARY KEY,
    partner_org_id    BIGINT NOT NULL REFERENCES partner_organizations(id),
    user_id           BIGINT NOT NULL UNIQUE REFERENCES users(id),
    joined_at         TIMESTAMP NOT NULL
);

CREATE TABLE support_faq_entries (
    id          BIGSERIAL PRIMARY KEY,
    question    VARCHAR(200) NOT NULL,
    answer      VARCHAR(1000) NOT NULL,
    keywords    VARCHAR(300) NOT NULL
);

-- Indexes on the columns that get filtered/joined most often across the services above
CREATE INDEX idx_profiles_locality ON profiles (locality);
CREATE INDEX idx_listings_locality_status ON listings (locality, verification_status);
CREATE INDEX idx_matches_requester ON matches (requester_id);
CREATE INDEX idx_matches_target ON matches (target_id);
CREATE INDEX idx_fraud_reports_target ON fraud_reports (target_type, target_id, status);