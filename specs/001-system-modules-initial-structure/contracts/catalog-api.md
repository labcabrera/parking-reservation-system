# Contract: Catalog Service REST API

**Service**: `catalog-service`
**Base URL**: `http://catalog-service/api/v1/catalog`
**Authentication**: Public (no token required for search and availability)
**Date**: 2026-06-01

---

## Endpoints

### `GET /search` — Search Parking Facilities

Search for available parking facilities matching the given criteria.

**Authentication**: None required (FR-006)

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | `string` | No | Free-text query (facility name, city, tag). Min 1 char if provided. |
| `checkIn` | `ISO 8601 datetime` | Yes | Desired check-in time (`YYYY-MM-DDTHH:mm:ssZ`) |
| `checkOut` | `ISO 8601 datetime` | Yes | Desired check-out time (`YYYY-MM-DDTHH:mm:ssZ`) |
| `lat` | `double` | No | Latitude for proximity search |
| `lng` | `double` | No | Longitude for proximity search |
| `radiusKm` | `double` | No | Radius in km for proximity filter (default: 10) |
| `type` | `string` | No | Spot type filter: `STANDARD`, `COMPACT`, `DISABLED`, `EV` |
| `tags` | `string[]` | No | Comma-separated facility tags: `COVERED`, `EV_CHARGING`, `FREE_CANCELLATION` |
| `page` | `int` | No | Zero-based page index (default: 0) |
| `size` | `int` | No | Page size, max 50 (default: 20) |

**Response `200 OK`**:

```json
{
  "content": [
    {
      "facilityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "Central Airport Parking T2",
      "city": "Madrid",
      "address": "Calle Aeropuerto, s/n, 28042 Madrid",
      "location": {
        "latitude": 40.4983,
        "longitude": -3.5676
      },
      "dailyRate": {
        "amount": "12.50",
        "currency": "EUR"
      },
      "availableSpots": 47,
      "totalSpots": 200,
      "lowAvailabilityWarning": false,
      "tags": ["COVERED", "EV_CHARGING"],
      "cancellationPolicy": {
        "freeCancelHours": 24,
        "penaltyCancelMinutes": 60
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

**`lowAvailabilityWarning`**: `true` when `availableSpots / totalSpots ≤ 0.10`
(configurable via `catalog.low-availability-threshold`).

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `400 Bad Request` | `INVALID_DATE_RANGE` | `checkOut` ≤ `checkIn` |
| `400 Bad Request` | `MISSING_REQUIRED_PARAMETER` | `checkIn` or `checkOut` absent |

---

### `GET /availability/stream` — Real-Time Availability Updates (SSE)

Server-Sent Events stream that pushes availability updates for a set of facilities.

**Authentication**: None required

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `facilityIds` | `string[]` | Yes | Comma-separated list of facility UUIDs to subscribe to |

**Response**: `text/event-stream`

Each event payload:
```
event: availability-update
data: {"facilityId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","availableSpots":46,"lowAvailabilityWarning":false}

event: availability-update
data: {"facilityId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","availableSpots":2,"lowAvailabilityWarning":true}
```

Clients MUST reconnect automatically (browser `EventSource` does this natively).
The server closes the connection after 30 minutes of inactivity; clients reconnect.

---

## Error Response Format (all endpoints)

```json
{
  "timestamp": "2026-06-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_DATE_RANGE",
  "message": "checkOut must be after checkIn",
  "path": "/api/v1/catalog/search",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

## Caching Behaviour (informational)

- Search results: cached in Redis with TTL 30 s, keyed on the canonicalised query hash.
- Availability counts: cached in Redis with TTL 60 s per `{facilityId}`.
- Cache is explicitly invalidated when the Catalog Service consumes a
  `ReservationConfirmedEvent` or `SpotReleasedEvent` from Kafka.
