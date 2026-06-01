# Contract: Catalog Service REST API — v2 Additions (Feature 002)

**Service**: `catalog-service`
**Base URL**: `http://catalog-service/api/v1/catalog`
**Authentication**: Public (no token required for search and availability)
**Date**: 2026-06-01
**Adds to**: [spec-001 catalog-api.md](../../001-system-modules-initial-structure/contracts/catalog-api.md)

---

## Changes to Existing Endpoints

### `GET /search` — Updated Response

The search response is extended with the following fields at the **envelope level**:

```json
{
  "searchSessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "stale": false,
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
      "estimatedPrice": {
        "amount": "37.50",
        "currency": "EUR"
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

#### New / Modified Fields

| Field | Location | Type | Description |
|-------|----------|------|-------------|
| `searchSessionId` | envelope | `string` (UUID) | Unique identifier for this search interaction. Client MUST store and forward on hold creation. |
| `stale` | envelope | `boolean` | `true` when results were served from cache because the primary persistence store was unavailable. Client SHOULD display a "results may not be current" notice when `stale: true`. |
| `totalElements` | envelope | `integer` | Total number of results matching the query (across all pages). |
| `totalPages` | envelope | `integer` | Total number of pages given the requested `size`. |
| `page` | envelope | `integer` | Zero-based page number of this response. |
| `size` | envelope | `integer` | Page size used for this response. |
| `estimatedPrice` | per result | `Money` | Estimated total price for the requested period (`dailyRate × days`). Indicative only — not a binding commitment. |
| `lowAvailability` | per result | `boolean` | `true` when the facility's available spots are below the server-configured low-availability threshold. Client SHOULD display a "limited availability" notice. (FR-006) |

#### Updated Query Parameters

The following parameters are **added** to the existing set (see spec-001 for full list):

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `lat` | `double` | No | Latitude for proximity search. Must be provided together with `lng`. |
| `lng` | `double` | No | Longitude for proximity search. Must be provided together with `lat`. |
| `radiusKm` | `double` | No | Radius in km. Requires `lat` + `lng`. Default: 10.0. Max: 500.0. |
| `page` | `integer` | No | Zero-based page number. Default: 0. (FR-004) |
| `size` | `integer` | No | Page size. Default: configurable server-side. Max: `catalog.search.max-page-size`. (FR-004) |
| `features` | `string[]` | No | Comma-separated or repeated param. Accepted values: `COVERED`, `EV_CHARGING`, `FREE_CANCELLATION`, `WHEELCHAIR_ACCESSIBLE`, `VALET`. Only facilities offering ALL requested features are returned. (FR-005) |

**Proximity filter rules**:
- If `lat` or `lng` is provided without the other → `400 MISSING_PROXIMITY_PAIR`
- If `lat`/`lng` provided without `radiusKm` → default radius of 10 km applies
- Free-text query (`q`) and proximity filter are combinable; both conditions are applied
  (intersection, not union)

**Pagination rules**:
- `page` < 0 → `400 INVALID_PAGE`
- `size` > `catalog.search.max-page-size` → clamped to max (no error)
- `size` ≤ 0 → `400 INVALID_SIZE`

**Feature filter rules**:
- Unknown feature value → `400 INVALID_FEATURE_VALUE`
- Multiple values are ANDed: a facility must have all requested features to appear in results

#### Updated Error Responses

| Status | Code | Condition |
|--------|------|-----------|
| `400 Bad Request` | `INVALID_DATE_RANGE` | `checkOut` ≤ `checkIn` |
| `400 Bad Request` | `MISSING_REQUIRED_PARAMETER` | `checkIn` or `checkOut` absent |
| `400 Bad Request` | `MISSING_PROXIMITY_PAIR` | Only one of `lat`/`lng` provided |
| `400 Bad Request` | `INVALID_RADIUS` | `radiusKm` ≤ 0 or > 500 |
| `400 Bad Request` | `INVALID_PAGE` | `page` < 0 |
| `400 Bad Request` | `INVALID_SIZE` | `size` ≤ 0 |
| `400 Bad Request` | `INVALID_FEATURE_VALUE` | Unrecognised value in `features` |
| `503 Service Unavailable` | `SEARCH_UNAVAILABLE` | Both DB and cache are unavailable |

---

## New Endpoints

### `GET /availability/stream` — Real-Time Availability (SSE)

Server-Sent Events stream for real-time availability changes.

**Authentication**: None required  
**Content-Type**: `text/event-stream`

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `facilityId` | `string` (UUID) | Yes | Facility to subscribe to |

**Response**: Continuous `text/event-stream`

```
event: availability-update
data: {"facilityId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","availableSpots":46,"lowAvailabilityWarning":false,"timestamp":"2026-06-01T10:05:00Z"}

: heartbeat

event: availability-update
data: {"facilityId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","availableSpots":2,"lowAvailabilityWarning":true,"timestamp":"2026-06-01T10:06:12Z"}
```

**Event payload fields**:

| Field | Type | Description |
|-------|------|-------------|
| `facilityId` | `string` (UUID) | Facility that changed |
| `availableSpots` | `integer` | Current available spot count |
| `lowAvailabilityWarning` | `boolean` | `true` when below configured threshold |
| `timestamp` | `string` (ISO 8601) | UTC timestamp of the change |

**Heartbeat**: A comment line (`: heartbeat`) is emitted every 30 s to keep the
connection alive across proxies.

**Reconnection**: SSE includes `retry: 5000` (5 s reconnect delay).

**Error handling**:
- Connection dropped on server restart → client reconnects via `retry`
- Invalid `facilityId` (not a valid UUID) → `400 Bad Request` before stream is established
