# Data Model: Parking Search & Hold Flow

**Phase**: 1 — Design
**Date**: 2026-06-01
**Feature**: [spec.md](spec.md) | [plan.md](plan.md) | [research.md](research.md)

---

## Bounded Context & Aggregate Ownership (this feature additions)

| Aggregate / Entity | Owner Service | Persistence |
|--------------------|---------------|-------------|
| `SpotHold` | reservation-service | PostgreSQL |
| `SearchSession` (logical) | catalog-service | **None** — UUID generated in-memory (R-007) |
| `EstimatedPrice` | catalog-service | Derived / not persisted |
| `ConfirmedPrice` | pricing-service | Derived / returned via Kafka |

Existing aggregates from spec-001:

| Aggregate / Entity | Owner Service | Persistence |
|--------------------|---------------|-------------|
| `ParkingFacility` | catalog-service | PostgreSQL |
| `ParkingSpot` | catalog-service | PostgreSQL |
| `Reservation` | reservation-service | PostgreSQL |
| `Payment` | payment-service | PostgreSQL |

---

## New: `SpotHold` Aggregate (reservation-service)

The `SpotHold` is an Axon 5.x `@Aggregate`. It represents the temporary lock placed
on one specific spot within a parking facility for a requested period.

### Fields

| Field | Type | Notes |
|-------|------|-------|
| `holdId` | `HoldId` (UUID) | `@AggregateIdentifier`; identity value object |
| `searchSessionId` | `String` (UUID) | Opaque correlation identifier from search |
| `facilityId` | `FacilityId` (UUID) | Reference to the locked facility |
| `spotId` | `SpotId` (UUID) | Reference to the specific locked spot |
| `userId` | `UserId` (UUID, nullable) | Null for anonymous holds; populated at checkout |
| `checkIn` | `Instant` | Start of requested period |
| `checkOut` | `Instant` | End of requested period (strictly after `checkIn`) |
| `status` | `HoldStatus` | Enum — see state machine below |
| `estimatedPrice` | `Money` | Price from search results (carry-forward) |
| `confirmedPrice` | `Money` (nullable) | Populated after pricing-service response |
| `expiresAt` | `Instant` | `checkIn` + TTL; TTL default 10 min |
| `pricingCorrelationId` | `String` (UUID, nullable) | Kafka correlation ID for pricing saga |
| `version` | `Long` | `@Version` — optimistic locking |

### `HoldStatus` Enum

```
PENDING_PRICE   — hold created, spot locked, awaiting confirmed price from pricing-service
ACTIVE          — confirmed price received; hold ready for checkout
EXPIRED         — TTL elapsed without visitor action; spot returned to inventory
RELEASED        — explicitly freed (new selection in same session); spot returned to inventory
FAILED          — pricing confirmation timed out or pricing-service error
CONVERTED       — visitor has advanced to a reservation; hold is superseded
```

### State Machine

```
                          ┌─────────────────────────┐
          CreateHold      │                         │  pricing timeout
  ───────────────────► PENDING_PRICE ──────────────► FAILED
                          │                         │
             pricing OK   │                         │
                          ▼                         │
                        ACTIVE ◄────────────────────┘
                          │
              ┌───────────┼───────────┐
              │           │           │
    TTL fires │  new sel. │  checkout │
              ▼           ▼           ▼
           EXPIRED    RELEASED    CONVERTED
```

Terminal states: `EXPIRED`, `RELEASED`, `FAILED`, `CONVERTED`

### Invariants

- A hold in `EXPIRED`, `RELEASED`, `FAILED`, or `CONVERTED` state MUST NOT be
  re-activated.
- A `CONVERTED` hold MUST NOT be released or cancelled via the hold endpoint.
- If a `searchSessionId` already has an `ACTIVE` or `PENDING_PRICE` hold, creating a
  new hold for the same session MUST first release the existing hold (`RELEASED` state).
- A spot (identified by `facilityId` + period `[checkIn, checkOut)`) MUST NOT be locked
  by more than one `ACTIVE` or `PENDING_PRICE` hold simultaneously (double-hold guard).

### Axon Commands

| Command | Transition | Notes |
|---------|------------|-------|
| `CreateHoldCommand` | → `PENDING_PRICE` | Publishes `HoldCreatedEvent` |
| `ConfirmHoldPriceCommand` | `PENDING_PRICE` → `ACTIVE` | Issued by saga on pricing result |
| `FailHoldPricingCommand` | `PENDING_PRICE` → `FAILED` | Issued by saga on timeout/error |
| `ReleaseHoldCommand` | `ACTIVE` / `PENDING_PRICE` → `RELEASED` | Explicit release by visitor or new selection |
| `ExpireHoldCommand` | `ACTIVE` → `EXPIRED` | Issued by Axon deadline handler |
| `ConvertHoldCommand` | `ACTIVE` → `CONVERTED` | Issued by reservation workflow |

### Axon Domain Events

| Event | Raised by | Consumers |
|-------|-----------|-----------|
| `HoldCreatedEvent` | `SpotHold` aggregate | `HoldPricingCoordinatorSaga` → publishes Kafka pricing request; catalog-service → updates availability cache |
| `HoldPriceConfirmedEvent` | `SpotHold` aggregate (via saga command) | Client polling |
| `HoldExpiredEvent` | `SpotHold` aggregate (deadline) | catalog-service → releases spot in availability |
| `HoldReleasedEvent` | `SpotHold` aggregate | catalog-service → releases spot in availability |
| `HoldConvertedEvent` | `SpotHold` aggregate | Reservation creation workflow |
| `HoldPricingFailedEvent` | `SpotHold` aggregate (via saga command) | Client polling |

---

## New Saga: `HoldPricingCoordinatorSaga` (reservation-service)

Coordinates the async Kafka round-trip for confirmed price calculation.

### Association: `holdId` ↔ `pricingCorrelationId`

```
[HoldCreatedEvent]
    → saga starts
    → publishes PricingRequestedEvent to Kafka (parking.pricing.requests)
    → schedules Axon deadline: pricing.confirmation.timeout-seconds (default: 10 s)

[PricingResultReceivedEvent] (from Kafka consumer → saga)
    → cancel deadline
    → send ConfirmHoldPriceCommand(holdId, confirmedPrice)
    → saga ends

[Axon deadline fires: "pricing-timeout"]
    → send FailHoldPricingCommand(holdId, reason="TIMEOUT")
    → saga ends
```

---

## Updates to Existing: `ParkingSpot` (catalog-service)

No new fields. The availability concurrency guard (double-hold prevention) is enforced
by the `SpotHold` aggregate in `reservation-service`, not by a lock on `ParkingSpot`.

`ParkingSpot.availabilityStatus` transitions driven by Kafka domain events:
- `HoldCreatedEvent` / `PENDING_PRICE` → status stays `AVAILABLE` (spot is soft-locked
  in hold, not hard-locked in spot entity — availability is virtual)
- `HoldExpiredEvent` / `HoldReleasedEvent` → no JPA update needed (availability is
  recomputed from active holds at query time)
- `HoldConvertedEvent` → triggers reservation creation; spot status may move to
  `RESERVED` as part of the reservation flow (out of scope for this spec)

> **Design note**: Availability for the search path is computed as:
> `availableSpots = totalSpots - count(ACTIVE holds + PENDING_PRICE holds for that
> facility+period)`. This query hits Redis cache; DB fallback on cache miss.

---

## DTO Additions

### `SearchResponse` (catalog-service, `application/dto`)

Add to existing response:

| Field | Type | Notes |
|-------|------|-------|
| `searchSessionId` | `String` (UUID) | Opaque session identifier (FR-003) |
| `stale` | `boolean` | `true` when results served from cache due to DB unavailability (FR-020) |
| `estimatedPrice` | `Money` | Total estimated price for requested period (FR-002) |
| `lowAvailabilityWarning` | `boolean` | Already exists — confirm threshold is configurable |

`Money` value object: `{ amount: BigDecimal, currency: String (ISO 4217) }`

### `HoldResponse` (reservation-service, `application/dto`)

New DTO returned by hold creation endpoint:

| Field | Type | Notes |
|-------|------|-------|
| `holdId` | `String` (UUID) | Unique hold identifier |
| `status` | `HoldStatus` | `PENDING_PRICE` immediately after creation |
| `facilityId` | `String` (UUID) | |
| `spotId` | `String` (UUID) | The locked spot |
| `searchSessionId` | `String` (UUID) | Echoed back |
| `checkIn` | `String` (ISO 8601) | |
| `checkOut` | `String` (ISO 8601) | |
| `estimatedPrice` | `Money` | Carry-forward from search |
| `confirmedPrice` | `Money` (nullable) | Populated when status = `ACTIVE` |
| `expiresAt` | `String` (ISO 8601) | Hold expiry time |

---

## Database Schema Additions

### New table: `spot_hold` (reservation-service)

```sql
CREATE TABLE spot_hold (
    hold_id           UUID        PRIMARY KEY,
    search_session_id UUID        NOT NULL,
    facility_id       UUID        NOT NULL,
    spot_id           UUID        NOT NULL,
    user_id           UUID,                          -- nullable: anonymous holds
    check_in          TIMESTAMPTZ NOT NULL,
    check_out         TIMESTAMPTZ NOT NULL,
    status            VARCHAR(20) NOT NULL,          -- HoldStatus enum
    estimated_amount  NUMERIC(10,2),
    estimated_currency VARCHAR(3),
    confirmed_amount  NUMERIC(10,2),
    confirmed_currency VARCHAR(3),
    expires_at        TIMESTAMPTZ NOT NULL,
    pricing_correlation_id UUID,
    version           BIGINT      NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for double-hold guard query
CREATE UNIQUE INDEX idx_spot_hold_active ON spot_hold (spot_id, check_in, check_out)
    WHERE status IN ('PENDING_PRICE', 'ACTIVE');

-- Index for session-based hold lookup (FR-011: release previous hold in session)
CREATE INDEX idx_spot_hold_session ON spot_hold (search_session_id, status)
    WHERE status IN ('PENDING_PRICE', 'ACTIVE');

-- Index for TTL sweep fallback job
CREATE INDEX idx_spot_hold_expiry ON spot_hold (expires_at)
    WHERE status IN ('PENDING_PRICE', 'ACTIVE');
```

Migration file: `reservation-service/src/main/resources/db/migration/V2__create_spot_hold.sql`

---

## Kafka Topics (this feature)

| Topic | Producer | Consumer | Message type |
|-------|----------|----------|-------------|
| `parking.pricing.requests` | reservation-service | pricing-service | `PricingRequestMessage` |
| `parking.pricing.results` | pricing-service | reservation-service | `PricingResultMessage` |
| `parking.availability.changes` | reservation-service | catalog-service | `SpotAvailabilityChangedMessage` |

### `PricingRequestMessage`
```json
{
  "correlationId": "uuid",
  "holdId": "uuid",
  "facilityId": "uuid",
  "spotId": "uuid",
  "checkIn": "ISO 8601",
  "checkOut": "ISO 8601",
  "baseRatePerDay": { "amount": "12.50", "currency": "EUR" }
}
```

### `PricingResultMessage`
```json
{
  "correlationId": "uuid",
  "holdId": "uuid",
  "success": true,
  "confirmedPrice": { "amount": "37.50", "currency": "EUR" },
  "errorCode": null
}
```

### `SpotAvailabilityChangedMessage`
```json
{
  "facilityId": "uuid",
  "spotId": "uuid",
  "changeType": "HOLD_CREATED | HOLD_RELEASED | HOLD_EXPIRED",
  "availableSpotsDelta": -1,
  "timestamp": "ISO 8601"
}
```
