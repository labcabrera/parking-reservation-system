# Contract: Reservation Service REST API

**Service**: `reservation-service`
**Base URL**: `http://reservation-service/api/v1/reservations`
**Authentication**: Bearer JWT (Keycloak-issued) required for all endpoints except where noted
**Roles**: `CUSTOMER`, `ADMINISTRATOR`
**Date**: 2026-06-01

---

## Endpoints

### `POST /` — Create Reservation

Creates a new reservation. Triggers the `ReservationPaymentSaga`.

**Authentication**: `CUSTOMER` or `ADMINISTRATOR`

**Request Headers**:

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | `Bearer <access_token>` |
| `Idempotency-Key` | Yes | Client-generated UUID; prevents duplicate reservation on retry |

**Request Body** (`application/json`):

```json
{
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "checkIn": "2026-07-01T09:00:00Z",
  "checkOut": "2026-07-03T18:00:00Z",
  "paymentMethodCode": "VISA",
  "requiresInvoice": false
}
```

**Response `201 Created`**:

```json
{
  "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "status": "PENDING",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "facilityName": "Central Airport Parking T2",
  "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "spotNumber": "A-042",
  "checkIn": "2026-07-01T09:00:00Z",
  "checkOut": "2026-07-03T18:00:00Z",
  "estimatedTotal": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "priceLockExpiresAt": "2026-06-01T12:15:00Z",
  "createdAt": "2026-06-01T12:00:00Z"
}
```

The response is returned immediately after the SAGA is started; the status will update
asynchronously to `CONFIRMED` or `REJECTED`. Clients SHOULD poll `GET /{id}` for the
final status.

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `400 Bad Request` | `INVALID_DATE_RANGE` | `checkOut` ≤ `checkIn` |
| `400 Bad Request` | `SPOT_NOT_AVAILABLE` | Spot already reserved for the requested period |
| `409 Conflict` | `DUPLICATE_RESERVATION` | Same `Idempotency-Key` already processed |
| `422 Unprocessable Entity` | `SPOT_NOT_FOUND` | Requested `spotId` does not exist |
| `422 Unprocessable Entity` | `FACILITY_NOT_FOUND` | Requested `facilityId` does not exist |

---

### `GET /{reservationId}` — Get Reservation Detail

Returns the current state of a reservation.

**Authentication**: `CUSTOMER` (own reservations only), `ADMINISTRATOR` (any)

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `reservationId` | UUID | Reservation identifier |

**Response `200 OK`**:

```json
{
  "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "status": "CONFIRMED",
  "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "facilityName": "Central Airport Parking T2",
  "facilityAddress": "Calle Aeropuerto, s/n, 28042 Madrid",
  "spotId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "spotNumber": "A-042",
  "checkIn": "2026-07-01T09:00:00Z",
  "checkOut": "2026-07-03T18:00:00Z",
  "totalCharged": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "paymentId": "a3bb189e-8bf9-3888-9912-ace4e6543002",
  "cancellationPolicy": {
    "freeCancelHours": 24,
    "freeCancelDeadline": "2026-06-30T09:00:00Z",
    "penaltyAmount": {
      "amount": "12.50",
      "currency": "EUR"
    }
  },
  "createdAt": "2026-06-01T12:00:00Z",
  "updatedAt": "2026-06-01T12:01:30Z"
}
```

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `403 Forbidden` | `ACCESS_DENIED` | CUSTOMER attempting to access another user's reservation |
| `404 Not Found` | `RESERVATION_NOT_FOUND` | Reservation does not exist |

---

### `DELETE /{reservationId}` — Cancel Reservation

Cancels a confirmed or pending reservation. Applies the cancellation policy.

**Authentication**: `CUSTOMER` (own reservations only), `ADMINISTRATOR` (any)

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `reservationId` | UUID | Reservation identifier |

**Request Body** (`application/json`, optional):

```json
{
  "reason": "Change of plans"
}
```

**Response `200 OK`**:

```json
{
  "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "status": "CANCELLED",
  "refundAmount": {
    "amount": "37.50",
    "currency": "EUR"
  },
  "penaltyApplied": false,
  "cancelledAt": "2026-06-28T10:00:00Z"
}
```

When a late-cancel penalty applies:
```json
{
  "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "status": "CANCELLED",
  "refundAmount": {
    "amount": "25.00",
    "currency": "EUR"
  },
  "penaltyApplied": true,
  "penaltyAmount": {
    "amount": "12.50",
    "currency": "EUR"
  },
  "cancelledAt": "2026-07-01T07:30:00Z"
}
```

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `403 Forbidden` | `ACCESS_DENIED` | CUSTOMER attempting to cancel another user's reservation |
| `404 Not Found` | `RESERVATION_NOT_FOUND` | Reservation does not exist |
| `409 Conflict` | `ALREADY_TERMINAL` | Reservation is already in CONFIRMED/CANCELLED/EXPIRED |
| `409 Conflict` | `CANCELLATION_NOT_ALLOWED` | Reservation status does not permit cancellation |

---

### `GET /` — List My Reservations (History)

Returns a paginated list of the authenticated user's reservations.

**Authentication**: `CUSTOMER` or `ADMINISTRATOR`
- `CUSTOMER`: returns only own reservations
- `ADMINISTRATOR`: optionally filter by `userId` query parameter

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | `string` | No | Filter by status: PENDING, CONFIRMED, CANCELLED, REJECTED, EXPIRED |
| `userId` | `UUID` | No | `ADMINISTRATOR` only — filter by Keycloak subject |
| `from` | `ISO 8601 date` | No | Filter reservations with `checkIn` ≥ this date |
| `to` | `ISO 8601 date` | No | Filter reservations with `checkIn` ≤ this date |
| `page` | `int` | No | Zero-based page index (default: 0) |
| `size` | `int` | No | Page size, max 50 (default: 20) |
| `sort` | `string` | No | Sort field: `createdAt`, `checkIn` (default: `createdAt,desc`) |

**Response `200 OK`**:

```json
{
  "content": [
    {
      "reservationId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
      "status": "CONFIRMED",
      "facilityName": "Central Airport Parking T2",
      "spotNumber": "A-042",
      "checkIn": "2026-07-01T09:00:00Z",
      "checkOut": "2026-07-03T18:00:00Z",
      "totalCharged": {
        "amount": "37.50",
        "currency": "EUR"
      },
      "createdAt": "2026-06-01T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

---

## Error Response Format (all endpoints)

```json
{
  "timestamp": "2026-06-01T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "SPOT_NOT_AVAILABLE",
  "message": "Spot 7c9e6679 is already reserved for the requested period",
  "path": "/api/v1/reservations",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```
