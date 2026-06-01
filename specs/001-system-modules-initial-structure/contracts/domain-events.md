# Contract: Domain Events (Kafka)

**Date**: 2026-06-01
**Feature**: [spec.md](spec.md) | [plan.md](plan.md)
**Serialisation**: JSON (UTF-8)
**Schema versioning**: Field additions are backwards-compatible; breaking changes MUST
bump the `eventType` (e.g., `ReservationCreatedEventV2`).

---

## Kafka Topics

| Topic | Partitions | Retention | Producer | Consumers |
|-------|-----------|-----------|----------|-----------|
| `parking.reservations` | 6 | 7 days | reservation-service | catalog-service |
| `parking.payments.requests` | 6 | 7 days | reservation-service (SAGA) | payment-service |
| `parking.payments.results` | 6 | 7 days | payment-service | reservation-service (SAGA) |

All topics use **`reservationId`** (or `facilityId` for catalog events) as the Kafka
partition key to ensure ordering per aggregate instance.

---

## Event Envelope

All events share a common envelope:

```json
{
  "eventId": "UUID",
  "eventType": "string",
  "aggregateId": "UUID",
  "aggregateType": "string",
  "occurredAt": "ISO 8601 UTC",
  "version": 1,
  "payload": { ... }
}
```

---

## Topic: `parking.reservations`

### `ReservationCreatedEvent`

Published when a new `Reservation` aggregate is created (status: PENDING, spot locked).

```json
{
  "eventId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
  "eventType": "ReservationCreatedEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-01T12:00:00Z",
  "version": 1,
  "payload": {
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "userId": "8e6b3b1a-4c8b-4c8b-b3b1-4c8b4c8b4c8b",
    "checkIn": "2026-07-01T09:00:00Z",
    "checkOut": "2026-07-03T18:00:00Z",
    "idempotencyKey": "client-uuid-abc123"
  }
}
```

---

### `PriceLockAcquiredEvent`

Published when the pricing service returns a confirmed price quote.

```json
{
  "eventType": "PriceLockAcquiredEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-01T12:00:01Z",
  "version": 1,
  "payload": {
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "lockedPrice": {
      "amount": "37.50",
      "currency": "EUR"
    },
    "expiresAt": "2026-06-01T12:15:00Z"
  }
}
```

---

### `ReservationConfirmedEvent`

Published when payment is approved and the reservation is in CONFIRMED state.

```json
{
  "eventType": "ReservationConfirmedEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-01T12:01:30Z",
  "version": 1,
  "payload": {
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "paymentId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
    "confirmedAt": "2026-06-01T12:01:30Z"
  }
}
```

**Catalog-Service reaction**: Decrement available spot count; invalidate Redis cache for
`facilityId`.

---

### `ReservationRejectedEvent`

Published when payment is declined or a concurrency conflict prevents confirmation.

```json
{
  "eventType": "ReservationRejectedEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-01T12:01:30Z",
  "version": 1,
  "payload": {
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "reason": "PAYMENT_DECLINED",
    "rejectedAt": "2026-06-01T12:01:30Z"
  }
}
```

**Catalog-Service reaction**: Release spot; increment available count; invalidate cache.

---

### `ReservationCancelledEvent`

Published when a user or administrator cancels a confirmed reservation.

```json
{
  "eventType": "ReservationCancelledEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-28T10:00:00Z",
  "version": 1,
  "payload": {
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "reason": "USER_REQUEST",
    "penaltyApplied": false,
    "cancelledAt": "2026-06-28T10:00:00Z"
  }
}
```

**Catalog-Service reaction**: Release spot; increment available count; invalidate cache.

---

### `ReservationExpiredEvent`

Published when the price-lock deadline fires and the SAGA transitions the reservation
to EXPIRED.

```json
{
  "eventType": "ReservationExpiredEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-01T12:15:00Z",
  "version": 1,
  "payload": {
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "expiredAt": "2026-06-01T12:15:00Z"
  }
}
```

**Catalog-Service reaction**: Release spot; increment available count; invalidate cache.

---

## Topic: `parking.payments.requests`

### `PaymentRequestedEvent`

Published by the `ReservationPaymentSaga` to initiate asynchronous payment processing.

```json
{
  "eventType": "PaymentRequestedEvent",
  "aggregateId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "aggregateType": "Reservation",
  "occurredAt": "2026-06-01T12:00:02Z",
  "version": 1,
  "payload": {
    "paymentId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "userId": "8e6b3b1a-4c8b-4c8b-b3b1-4c8b4c8b4c8b",
    "amount": {
      "amount": "37.50",
      "currency": "EUR"
    },
    "methodCode": "VISA",
    "idempotencyKey": "payment-idem-xyz789"
  }
}
```

---

## Topic: `parking.payments.results`

### `PaymentApprovedEvent`

```json
{
  "eventType": "PaymentApprovedEvent",
  "aggregateId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
  "aggregateType": "Payment",
  "occurredAt": "2026-06-01T12:01:28Z",
  "version": 1,
  "payload": {
    "paymentId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "externalReference": "GW-2026-0123456",
    "approvedAt": "2026-06-01T12:01:28Z"
  }
}
```

**Reservation-Service SAGA reaction**: Transitions `Reservation` to CONFIRMED; cancels
price-lock deadline.

---

### `PaymentDeclinedEvent`

```json
{
  "eventType": "PaymentDeclinedEvent",
  "aggregateId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
  "aggregateType": "Payment",
  "occurredAt": "2026-06-01T12:01:28Z",
  "version": 1,
  "payload": {
    "paymentId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
    "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "reason": "INSUFFICIENT_FUNDS",
    "declinedAt": "2026-06-01T12:01:28Z"
  }
}
```

**Reservation-Service SAGA reaction**: Dispatches compensating `CancelReservationCommand`;
spot released; reservation transitions to REJECTED.

---

## Consumer Group IDs

| Consumer | Group ID |
|----------|----------|
| catalog-service (reservation events) | `catalog-service-reservation-consumer` |
| payment-service (payment requests) | `payment-service-request-consumer` |
| reservation-service SAGA (payment results) | `reservation-service-payment-result-consumer` |

All consumer groups use `auto.offset.reset=earliest` and `enable.auto.commit=false`.
Offsets are committed manually after successful business logic processing to ensure
at-least-once delivery.
