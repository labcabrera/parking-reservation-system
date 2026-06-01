-- V1: Catalog Service Initial Schema
-- Manages: parking_facility, parking_spot

-- Create schema (idempotent: safe to run on fresh databases)
CREATE SCHEMA IF NOT EXISTS catalog;

CREATE TABLE catalog.parking_facility (
    id              UUID PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    address         VARCHAR(500) NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    total_spots     INT NOT NULL CHECK (total_spots >= 1),
    tags            TEXT[] NOT NULL DEFAULT '{}',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'CLOSED')),
    free_cancel_hours       INT NOT NULL DEFAULT 24,
    penalty_cancel_minutes  INT NOT NULL DEFAULT 60,
    version         BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_parking_facility_city ON catalog.parking_facility (city);
CREATE INDEX idx_parking_facility_status ON catalog.parking_facility (status);

CREATE TABLE catalog.parking_spot (
    id                  UUID PRIMARY KEY,
    facility_id         UUID NOT NULL REFERENCES catalog.parking_facility (id),
    spot_number         VARCHAR(20) NOT NULL,
    type                VARCHAR(20) NOT NULL
                            CHECK (type IN ('STANDARD', 'COMPACT', 'DISABLED', 'EV')),
    availability_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                            CHECK (availability_status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE')),
    version             BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_parking_spot_facility_id ON catalog.parking_spot (facility_id);
CREATE INDEX idx_parking_spot_status ON catalog.parking_spot (availability_status);
CREATE UNIQUE INDEX uq_parking_spot_number ON catalog.parking_spot (facility_id, spot_number);
