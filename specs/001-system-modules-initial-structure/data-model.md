# Data Model: System Modules Definition & Initial Structure

**Phase**: 1 — Design
**Date**: 2026-06-01
**Feature**: [spec.md](spec.md) | [plan.md](plan.md) | [research.md](research.md)

---

## Bounded Contexts & Aggregate Ownership

| Aggregate / Entity | Owner Service | Persistence |
|--------------------|---------------|-------------|
| `ParkingFacility` | catalog-service | PostgreSQL |
| `ParkingSpot` | catalog-service | PostgreSQL |
| `Reservation` | reservation-service | PostgreSQL |
| `UserProfile` | reservation-service | PostgreSQL |
| `Payment` | payment-service | PostgreSQL |

---

## Catalog Service Domain Model

### Aggregate Root: `ParkingFacility`

Represents a physical parking location (airport terminal, city car park, etc.).

| Field | Type | Notes |
|-------|------|-------|
| `id` | `FacilityId` (UUID) | Identity, value object |
| `name` | `String` | Required, max 255 chars |
| `city` | `String` | Required |
| `address` | `String` | Required |
| `location` | `Coordinates` | Value object — latitude + longitude |
| `totalSpots` | `int` | Total spot count (denormalised from ParkingSpot) |
| `tags` | `Set<FacilityTag>` | Enum: EXPRESS_ENTRY, FREE_CANCELLATION, COVERED, EV_CHARGING, etc. |
| `status` | `FacilityStatus` | Enum: ACTIVE, MAINTENANCE, CLOSED |
| `cancellationPolicy` | `CancellationPolicy` | Value object (freeCancelHours, penaltyCancelMinutes) |
| `version` | `Long` | `@Version` — optimistic locking |

**Invariants**:
- `totalSpots` ≥ 1
- A facility in `CLOSED` status MUST NOT appear in search results

### Entity: `ParkingSpot`

An individual bookable space within a `ParkingFacility`.

| Field | Type | Notes |
|-------|------|-------|
| `id` | `SpotId` (UUID) | Identity, value object |
| `facilityId` | `FacilityId` | Parent reference |
| `spotNumber` | `String` | Human-readable label (e.g., "A-042") |
| `type` | `SpotType` | Enum: STANDARD, COMPACT, DISABLED, EV |
| `availabilityStatus` | `SpotAvailabilityStatus` | Enum: AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE |
| `version` | `Long` | `@Version` — optimistic locking (concurrency guard) |

**State transitions**:
```
AVAILABLE → RESERVED   (reservation created — spot locked)
RESERVED  → AVAILABLE  (reservation cancelled / expired — spot released)
RESERVED  → OCCUPIED   (check-in confirmed)
OCCUPIED  → AVAILABLE  (check-out)
ANY       → MAINTENANCE
MAINTENANCE → AVAILABLE
```

### Value Objects (Catalog)

| Value Object | Fields | Notes |
|--------------|--------|-------|
| `FacilityId` | `UUID value` | Immutable |
| `SpotId` | `UUID value` | Immutable |
| `Coordinates` | `double latitude`, `double longitude` | Immutable |
| `CancellationPolicy` | `int freeCancelHours`, `int penaltyCancelMinutes` | Immutable |
| `AvailabilityWindow` | `LocalDateTime checkIn`, `LocalDateTime checkOut` | Immutable; validates checkOut > checkIn |

---

## Reservation Service Domain Model

### Aggregate Root: `Reservation` (Axon `@Aggregate`)

Owns the full reservation lifecycle and enforces the overselling invariant.

| Field | Type | Notes |
|-------|------|-------|
| `id` | `ReservationId` (UUID) | Identity, value object |
| `spotId` | `SpotId` | Reference to the booked spot |
| `facilityId` | `FacilityId` | Denormalised for query convenience |
| `userId` | `String` | Keycloak subject (external identity reference) |
| `period` | `ReservationPeriod` | Value object |
| `priceLock` | `PriceLock` | Value object; null until price quote obtained |
| `status` | `ReservationStatus` | State machine below |
| `cancellationReason` | `String` | Populated on CANCELLED / REJECTED |
| `idempotencyKey` | `String` | Client-supplied; enforced unique per user |
| `version` | `Long` | `@Version` — optimistic locking |

**State machine**:
```
              ┌─────────────────┐
              │     PENDING     │◄── created (spot locked)
              └────────┬────────┘
       ┌───────────────┼───────────────┬──────────────┐
       ▼               ▼               ▼              ▼
  CONFIRMED       REJECTED         CANCELLED       EXPIRED
  (payment OK)  (payment fail /   (user cancel)  (price-lock
                 spot conflict)                   deadline)
```
All terminal states MUST release the spot lock (SpotAvailabilityStatus → AVAILABLE).

**Domain events raised**:
- `ReservationCreatedEvent`
- `PriceLockAcquiredEvent`
- `ReservationConfirmedEvent`
- `ReservationRejectedEvent`
- `ReservationCancelledEvent`
- `ReservationExpiredEvent`

### Value Objects (Reservation)

| Value Object | Fields | Notes |
|--------------|--------|-------|
| `ReservationId` | `UUID value` | Immutable |
| `ReservationPeriod` | `LocalDateTime checkIn`, `LocalDateTime checkOut` | Immutable; checkOut > checkIn invariant |
| `PriceLock` | `Money price`, `Instant lockedAt`, `Instant expiresAt` | Immutable; `isExpired()` helper |
| `Money` | `BigDecimal amount`, `String currency` (ISO 4217) | Immutable; never negative |

### Entity: `UserProfile`

Stores non-identity user attributes. Identity (credentials, roles) is owned exclusively
by Keycloak.

| Field | Type | Notes |
|-------|------|-------|
| `id` | `UUID` | Internal identity |
| `keycloakSubject` | `String` | Keycloak `sub` claim — foreign key to identity |
| `fullName` | `String` | Encrypted at rest |
| `email` | `String` | Encrypted at rest; used only for internal reference |
| `vehiclePlate` | `String` | Encrypted at rest |
| `requiresInvoice` | `boolean` | |
| `marketingConsent` | `boolean` | |
| `termsAcceptedAt` | `Instant` | Mandatory; MUST NOT be null |
| `createdAt` | `Instant` | |

**Data-protection notes**:
- `fullName`, `email`, `vehiclePlate` MUST be encrypted at column level (AES-256) or via
  a transparent database encryption mechanism.
- These fields MUST NOT appear in any log output, metrics label, or event payload.

---

## Payment Service Domain Model

### Aggregate Root: `Payment` (Axon `@Aggregate`)

| Field | Type | Notes |
|-------|------|-------|
| `id` | `PaymentId` (UUID) | Identity |
| `reservationId` | `ReservationId` | Associated reservation |
| `method` | `PaymentMethod` | Value object |
| `amount` | `Money` | Agreed price at time of payment request |
| `status` | `PaymentStatus` | Enum: PENDING → APPROVED \| DECLINED |
| `externalReference` | `String` | Mock gateway transaction ID |
| `processedAt` | `Instant` | |
| `idempotencyKey` | `String` | Prevents duplicate payment processing on retry |
| `version` | `Long` | `@Version` |

**Domain events raised**:
- `PaymentInitiatedEvent`
- `PaymentApprovedEvent`
- `PaymentDeclinedEvent`

### Value Objects (Payment)

| Value Object | Fields | Notes |
|--------------|--------|-------|
| `PaymentId` | `UUID value` | Immutable |
| `PaymentMethod` | `String methodCode`, `String displayName` | e.g., VISA, MASTERCARD, PAYPAL |

---

## Cross-Service Event Contracts (Summary)

Detailed schemas in [contracts/domain-events.md](contracts/domain-events.md).

| Event | Producer | Consumer(s) | Kafka Topic |
|-------|----------|-------------|-------------|
| `ReservationCreatedEvent` | reservation-service | payment-service | `parking.reservations` |
| `ReservationConfirmedEvent` | reservation-service | catalog-service (invalidate cache) | `parking.reservations` |
| `ReservationExpiredEvent` | reservation-service | catalog-service (release spot, invalidate cache) | `parking.reservations` |
| `ReservationCancelledEvent` | reservation-service | catalog-service | `parking.reservations` |
| `PaymentInitiatedEvent` | reservation-service (SAGA) | payment-service | `parking.payments` |
| `PaymentApprovedEvent` | payment-service | reservation-service (SAGA) | `parking.payments` |
| `PaymentDeclinedEvent` | payment-service | reservation-service (SAGA) | `parking.payments` |

---

## Persistence Schema Notes

- Each service manages its own schema within its own PostgreSQL database (or schema
  namespace in the shared development Postgres container).
- All tables MUST have `created_at TIMESTAMPTZ NOT NULL DEFAULT now()` and
  `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`.
- Flyway manages all schema migrations; no manual DDL in production.
- The `outbox_events` table (see research R-003) exists in both `reservation-service` and
  `payment-service` schemas.
