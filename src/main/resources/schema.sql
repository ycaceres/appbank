CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT PRIMARY KEY,
    transaction_id  VARCHAR(20) NOT NULL,
    account_id      VARCHAR(20) NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
