CREATE SCHEMA IF NOT EXISTS ecommerce;

CREATE TABLE IF NOT EXISTS ecommerce.orders (
    id UUID PRIMARY KEY,
    hold_id UUID NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_payment_attempt_id UUID,
    failure_reason VARCHAR(500),
    version BIGINT
);

CREATE INDEX IF NOT EXISTS ix_orders_hold_id
    ON ecommerce.orders (hold_id);

CREATE INDEX IF NOT EXISTS ix_orders_status_expires_at
    ON ecommerce.orders (status, expires_at);
