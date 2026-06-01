-- V1: Payment Service Initial Schema
-- Manages: payment, outbox_events

CREATE TABLE payment.payment (
    id                  UUID PRIMARY KEY,
    reservation_id      UUID NOT NULL,
    method_code         VARCHAR(50) NOT NULL,
    method_display_name VARCHAR(100) NOT NULL,
    amount              NUMERIC(10, 2) NOT NULL,
    currency            CHAR(3) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED')),
    external_reference  VARCHAR(255),
    idempotency_key     VARCHAR(255) NOT NULL UNIQUE,
    processed_at        TIMESTAMPTZ,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_reservation_id ON payment.payment (reservation_id);
CREATE INDEX idx_payment_status ON payment.payment (status);

CREATE TABLE payment.outbox_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    payload         JSONB NOT NULL,
    topic           VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at         TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status ON payment.outbox_events (status, created_at);
