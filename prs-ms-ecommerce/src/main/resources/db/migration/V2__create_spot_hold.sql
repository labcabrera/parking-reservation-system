-- V2: Spot Hold Table
-- Manages the temporary spot hold aggregate for the shopping session flow.
-- States: PENDING_PRICE → ACTIVE → EXPIRED | RELEASED | CONVERTED
--         PENDING_PRICE → FAILED

CREATE TABLE reservation.spot_hold (
    id                  UUID PRIMARY KEY,
    search_session_id   VARCHAR(36) NOT NULL,
    spot_id             UUID NOT NULL,
    facility_id         UUID NOT NULL,
    visitor_ip          VARCHAR(45),
    check_in            TIMESTAMPTZ NOT NULL,
    check_out           TIMESTAMPTZ NOT NULL,
    status              VARCHAR(20) NOT NULL
                            CHECK (status IN ('PENDING_PRICE','ACTIVE','EXPIRED','RELEASED','FAILED','CONVERTED')),
    estimated_price_amount   NUMERIC(10,2),
    estimated_price_currency CHAR(3),
    confirmed_price_amount   NUMERIC(10,2),
    confirmed_price_currency CHAR(3),
    expires_at          TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_hold_period CHECK (check_out > check_in)
);

-- Prevents double-holds: only one PENDING_PRICE or ACTIVE hold per spot per period.
CREATE UNIQUE INDEX idx_spot_hold_active
    ON reservation.spot_hold (spot_id, check_in, check_out)
    WHERE status IN ('PENDING_PRICE', 'ACTIVE');

CREATE INDEX idx_spot_hold_session ON reservation.spot_hold (search_session_id);
CREATE INDEX idx_spot_hold_status  ON reservation.spot_hold (status, expires_at);
