# Contract: Payment Service REST API

**Service**: `payment-service`
**Base URL**: `http://payment-service/api/v1/payments`
**Authentication**: Bearer JWT for human-facing endpoints; Kafka events for internal processing
**Date**: 2026-06-01

---

## Overview

The Payment Service simulates asynchronous payment processing. The happy path is fully
Kafka-driven (no synchronous REST call from the frontend to initiate payment — the
`ReservationPaymentSaga` in `reservation-service` dispatches payment commands via Kafka).
The REST API exposes only read/reference endpoints for frontend use.

---

## REST Endpoints

### `GET /methods` — List Supported Payment Methods

Returns the list of supported payment methods available for selection at checkout.

**Authentication**: `CUSTOMER` or `ADMINISTRATOR`

**Response `200 OK`**:

```json
{
  "methods": [
    {
      "code": "VISA",
      "displayName": "Visa",
      "iconUrl": "/assets/payment-icons/visa.svg"
    },
    {
      "code": "MASTERCARD",
      "displayName": "Mastercard",
      "iconUrl": "/assets/payment-icons/mastercard.svg"
    },
    {
      "code": "PAYPAL",
      "displayName": "PayPal",
      "iconUrl": "/assets/payment-icons/paypal.svg"
    }
  ]
}
```

---

### `GET /{paymentId}` — Get Payment Detail

Returns the current state of a payment record.

**Authentication**: `CUSTOMER` (own payments only — verified by linked reservationId + userId), `ADMINISTRATOR` (any)

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `paymentId` | UUID | Payment identifier |

**Response `200 OK`**:

```json
{
  "paymentId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
  "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "method": {
    "code": "VISA",
    "displayName": "Visa"
  },
  "amount": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "status": "APPROVED",
  "externalReference": "GW-2026-0123456",
  "processedAt": "2026-06-01T12:01:28Z"
}
```

**Payment statuses**:

| Status | Description |
|--------|-------------|
| `PENDING` | Payment initiated, awaiting gateway response |
| `APPROVED` | Payment successfully processed |
| `DECLINED` | Payment declined by gateway (insufficient funds, fraud, etc.) |

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `403 Forbidden` | `ACCESS_DENIED` | CUSTOMER attempting to view another user's payment |
| `404 Not Found` | `PAYMENT_NOT_FOUND` | Payment record does not exist |

---

## Internal Kafka-Based Flow

The actual payment processing is asynchronous and event-driven. The REST POST `/` is
intentionally **absent** — payment initiation is triggered exclusively via Kafka events
by the `ReservationPaymentSaga`.

```
reservation-service                 payment-service
        │                                  │
        │  [parking.payments.requests]      │
        │  PaymentRequestedEvent ──────────►│
        │                                  │ (simulated processing: 1–3s random delay)
        │                                  │ (random APPROVED / DECLINED outcome)
        │  [parking.payments.results]       │
        │◄────────── PaymentApprovedEvent  │
        │       or   PaymentDeclinedEvent  │
```

**Kafka topics**: see [domain-events.md](domain-events.md)

**Simulation behaviour** (configurable, for integration testing):
- `payment.simulator.success-rate` (default: `0.85`) — probability of APPROVED outcome
- `payment.simulator.min-delay-ms` (default: `500`) — minimum processing delay
- `payment.simulator.max-delay-ms` (default: `3000`) — maximum processing delay

---

## Error Response Format

```json
{
  "timestamp": "2026-06-01T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "PAYMENT_NOT_FOUND",
  "message": "Payment a3bb189e not found",
  "path": "/api/v1/payments/a3bb189e-8bf9-3888-9912-ace4e6543002",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```
