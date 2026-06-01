# Contract: Domain Events — Feature 002 (Parking Search & Hold)

**Date**: 2026-06-01
**Adds to**: [spec-001 domain-events.md](../../001-system-modules-initial-structure/contracts/domain-events.md)

---

## New Domain Events

### `HoldCreatedEvent`

**Producer**: reservation-service (`SpotHold` aggregate)  
**Consumers**: `HoldPricingCoordinatorSaga` (internal), catalog-service (Kafka consumer — availability update)

```json
{
  "eventType": "HoldCreatedEvent",
  "holdId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "searchSessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "spotId": "9d4e1234-5678-90ab-cdef-1234567890ab",
  "checkIn": "2026-07-01T10:00:00Z",
  "checkOut": "2026-07-04T10:00:00Z",
  "expiresAt": "2026-06-01T10:15:00Z",
  "occurredAt": "2026-06-01T10:05:00Z"
}
```

---

### `HoldPriceConfirmedEvent`

**Producer**: reservation-service (`SpotHold` aggregate, via `HoldPricingCoordinatorSaga`)  
**Consumers**: API response polling (GET /holds/{holdId})

```json
{
  "eventType": "HoldPriceConfirmedEvent",
  "holdId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "confirmedPrice": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "occurredAt": "2026-06-01T10:05:01Z"
}
```

---

### `HoldExpiredEvent`

**Producer**: reservation-service (`SpotHold` aggregate, Axon deadline handler)  
**Consumers**: catalog-service (Kafka — releases spot in availability)

```json
{
  "eventType": "HoldExpiredEvent",
  "holdId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "spotId": "9d4e1234-5678-90ab-cdef-1234567890ab",
  "checkIn": "2026-07-01T10:00:00Z",
  "checkOut": "2026-07-04T10:00:00Z",
  "occurredAt": "2026-06-01T10:15:02Z"
}
```

---

### `HoldReleasedEvent`

**Producer**: reservation-service (`SpotHold` aggregate)  
**Consumers**: catalog-service (Kafka — releases spot in availability)

```json
{
  "eventType": "HoldReleasedEvent",
  "holdId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "spotId": "9d4e1234-5678-90ab-cdef-1234567890ab",
  "releasedBy": "NEW_SELECTION",
  "occurredAt": "2026-06-01T10:08:00Z"
}
```

`releasedBy` values: `NEW_SELECTION` (visitor chose different parking), `EXPLICIT` (DELETE /holds/{holdId})

---

### `HoldConvertedEvent`

**Producer**: reservation-service (`SpotHold` aggregate)  
**Consumers**: reservation creation workflow (future spec)

```json
{
  "eventType": "HoldConvertedEvent",
  "holdId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "spotId": "9d4e1234-5678-90ab-cdef-1234567890ab",
  "confirmedPrice": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "occurredAt": "2026-06-01T10:09:00Z"
}
```

---

### `HoldPricingFailedEvent`

**Producer**: reservation-service (`SpotHold` aggregate, via saga timeout handler)  
**Consumers**: API response polling (GET /holds/{holdId})

```json
{
  "eventType": "HoldPricingFailedEvent",
  "holdId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "reason": "TIMEOUT",
  "occurredAt": "2026-06-01T10:05:11Z"
}
```

`reason` values: `TIMEOUT`, `PRICING_SERVICE_ERROR`

---

## Kafka Message Schemas (non-Axon)

### Topic: `parking.pricing.requests`

**Producer**: reservation-service  
**Consumer**: pricing-service

```json
{
  "correlationId": "uuid",
  "holdId": "uuid",
  "facilityId": "uuid",
  "spotId": "uuid",
  "checkIn": "ISO 8601",
  "checkOut": "ISO 8601",
  "baseRatePerDay": {
    "amount": "12.50",
    "currency": "EUR"
  }
}
```

### Topic: `parking.pricing.results`

**Producer**: pricing-service  
**Consumer**: reservation-service

```json
{
  "correlationId": "uuid",
  "holdId": "uuid",
  "success": true,
  "confirmedPrice": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "errorCode": null
}
```

`errorCode` values (when `success: false`): `CALCULATION_ERROR`, `FACILITY_NOT_FOUND`

### Topic: `parking.availability.changes`

**Producer**: reservation-service (publishes on hold create / expire / release)  
**Consumer**: catalog-service (updates SSE stream + cache invalidation)

```json
{
  "facilityId": "uuid",
  "spotId": "uuid",
  "changeType": "HOLD_CREATED | HOLD_RELEASED | HOLD_EXPIRED",
  "availableSpotsDelta": -1,
  "timestamp": "ISO 8601"
}
```
