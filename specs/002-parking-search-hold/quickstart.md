# Developer Quickstart: Feature 002 — Parking Search & Hold

**Branch**: `002-parking-search-hold`
**Date**: 2026-06-01

---

## Prerequisites

- Java 21 (`sdk use java 21.x.x-tem` or equivalent)
- Docker & Docker Compose (for `docker-compose-infra.yaml`)
- Node 20 LTS + npm (for frontend)
- Gradle (use `./gradlew` wrapper — do not install globally)

---

## Start Infrastructure

```bash
# From repo root: start Kafka, PostgreSQL, Redis, Keycloak, OTEL Collector
docker compose -f docker-compose-infra.yaml up -d

# Verify Kafka topics are created (wait ~10 s for broker to be ready)
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Expected topics after init script runs:
- `parking.pricing.requests`
- `parking.pricing.results`
- `parking.availability.changes`
- (existing topics from spec-001)

---

## Build & Run Services

```bash
# Build all modules
./gradlew build -x test

# Run catalog-service (port 8081)
./gradlew :catalog-service:bootRun

# Run reservation-service (port 8082)
./gradlew :reservation-service:bootRun

# Run pricing-service (port 8083) — NEW in this feature
./gradlew :pricing-service:bootRun
```

### Run with full Docker Compose stack

```bash
docker compose up --build
```

---

## Key Configuration Properties

### catalog-service `application.yml`

```yaml
catalog:
  search:
    max-results: 50                         # FR-004 configurable page size
    low-availability-threshold: 0.10        # FR-006 low availability flag (10%)
  cache:
    search:
      ttl-seconds: 300                      # R-006 cache TTL for search results
```

### reservation-service `application.yml`

```yaml
reservation:
  hold:
    ttl-minutes: 10                         # FR-009 hold TTL (default 10 min)
  pricing:
    confirmation-timeout-seconds: 10        # R-002 pricing saga timeout

resilience4j:
  ratelimiter:
    instances:
      hold-creation-by-ip:
        limit-for-period: 5
        limit-refresh-period: 60s
        timeout-duration: 0s
      hold-creation-by-session:
        limit-for-period: 3
        limit-refresh-period: 60s
        timeout-duration: 0s
```

### pricing-service `application.yml`

```yaml
spring:
  kafka:
    consumer:
      group-id: pricing-service
      topics:
        pricing-requests: parking.pricing.requests
    producer:
      topics:
        pricing-results: parking.pricing.results
```

---

## Testing the Flow Manually

### 1. Search for parking

```bash
curl -s "http://localhost:8081/api/v1/catalog/search?q=Madrid&checkIn=2026-07-01T10:00:00Z&checkOut=2026-07-04T10:00:00Z" | jq .
```

Expected: response includes `searchSessionId`, `estimatedPrice` per facility, `stale: false`.

### 2. Create a hold

```bash
curl -s -X POST http://localhost:8082/api/v1/reservations/holds \
  -H "Content-Type: application/json" \
  -d '{
    "searchSessionId": "<searchSessionId from step 1>",
    "facilityId": "<facilityId from step 1>",
    "checkIn": "2026-07-01T10:00:00Z",
    "checkOut": "2026-07-04T10:00:00Z"
  }' | jq .
```

Expected: `202 Accepted` with `status: PENDING_PRICE`.

### 3. Poll for confirmed price

```bash
curl -s "http://localhost:8082/api/v1/reservations/holds/<holdId>" | jq .
```

Poll until `status: ACTIVE` and `confirmedPrice` is populated (typically within 1–2 s).

### 4. Subscribe to SSE stream

```bash
curl -s -N "http://localhost:8081/api/v1/catalog/availability/stream?facilityId=<facilityId>"
```

Events are pushed when holds are created/released on that facility.

---

## Running Tests

```bash
# All tests (requires running infra for integration tests)
./gradlew test

# Unit tests only (no infra required)
./gradlew test -Ptest.integration.skip=true

# Specific module
./gradlew :catalog-service:test
./gradlew :reservation-service:test
./gradlew :pricing-service:test
```

---

## Key Implementation Files (this feature)

| Service | File | Purpose |
|---------|------|---------|
| catalog-service | `SearchParkingQueryHandler.java` | Add `searchSessionId`, `estimatedPrice`, degraded fallback |
| catalog-service | `SearchRequest.java` | Add `lat`, `lng`, `radiusKm` fields |
| catalog-service | `SearchResponse.java` | Add `searchSessionId`, `stale` fields |
| catalog-service | `FacilityResult.java` | Add `estimatedPrice` field |
| catalog-service | `CatalogController.java` | Add SSE endpoint |
| catalog-service | `AvailabilityStreamRegistry.java` | NEW — manages `SseEmitter` per facility |
| reservation-service | `SpotHold.java` | NEW — Axon 5.x `@Aggregate` with full state machine |
| reservation-service | `HoldController.java` | NEW — REST adapter for hold endpoints |
| reservation-service | `HoldPricingCoordinatorSaga.java` | NEW — Axon Saga |
| reservation-service | `V2__create_spot_hold.sql` | NEW — Flyway migration |
| reservation-service | `build.gradle` | Axon 4.10.3 → 5.x version bump |
| pricing-service | `PricingServiceApplication.java` | NEW — entire module scaffold |

---

## ArchUnit Enforcement

The existing `ArchitectureTest` class in each module verifies no cross-layer dependency
violations. After adding new classes, ensure:

1. `SpotHold` aggregate is in `domain/model/` — no Spring/JPA imports
2. `HoldController` is in `interfaces/rest/` — no domain imports except ports
3. Saga is in `application/saga/` — imports domain commands/events only
4. Kafka consumer adapters are in `infrastructure/messaging/`
