-- V1: Reservation Service Initial Schema
-- Manages: reservation, user_profile, outbox_events

CREATE TABLE reservation.user_profile (
    id                  UUID PRIMARY KEY,
    keycloak_subject    VARCHAR(255) NOT NULL UNIQUE,
    full_name           TEXT NOT NULL,          -- encrypted at rest (AES-256)
    email               TEXT NOT NULL,          -- encrypted at rest (AES-256)
    vehicle_plate       TEXT NOT NULL,          -- encrypted at rest (AES-256)
    requires_invoice    BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_consent   BOOLEAN NOT NULL DEFAULT FALSE,
    terms_accepted_at   TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_profile_keycloak_subject ON reservation.user_profile (keycloak_subject);

CREATE TABLE reservation.reservation (
    id                  UUID PRIMARY KEY,
    spot_id             UUID NOT NULL,
    facility_id         UUID NOT NULL,
    user_id             VARCHAR(255) NOT NULL,
    check_in            TIMESTAMPTZ NOT NULL,
    check_out           TIMESTAMPTZ NOT NULL,
    price_amount        NUMERIC(10, 2),
    price_currency      CHAR(3),
    price_locked_at     TIMESTAMPTZ,
    price_expires_at    TIMESTAMPTZ,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'EXPIRED')),
    cancellation_reason TEXT,
    idempotency_key     VARCHAR(255) NOT NULL,
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_period CHECK (check_out > check_in)
);

CREATE INDEX idx_reservation_user_id ON reservation.reservation (user_id);
CREATE INDEX idx_reservation_spot_id ON reservation.reservation (spot_id);
CREATE INDEX idx_reservation_status ON reservation.reservation (status);
CREATE UNIQUE INDEX uq_reservation_idempotency ON reservation.reservation (user_id, idempotency_key);

CREATE TABLE reservation.outbox_events (
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

CREATE INDEX idx_outbox_status ON reservation.outbox_events (status, created_at);
