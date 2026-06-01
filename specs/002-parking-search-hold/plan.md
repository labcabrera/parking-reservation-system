# Implementation Plan: Parking Search & Hold Flow (Shopping Session)

**Branch**: `002-parking-search-hold` | **Date**: 2026-06-01 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/002-parking-search-hold/spec.md`

## Summary

Implement the parking search-and-hold flow: an anonymous visitor searches for parking
facilities using free-text + optional proximity filters, receives paginated results
with estimated prices, feature filters, and a unique `searchSessionId`, then selects
a facility to create a temporary spot hold. The hold triggers an async Kafka round-trip
to a new `pricing-service` for a confirmed price, managed by an Axon 5.x saga. Holds
have a configurable TTL (default 10 min) enforced by Axon `DeadlineManager`. A
Server-Sent Events stream in `catalog-service` pushes real-time availability changes to
connected clients. Rate limiting protects the anonymous hold endpoint. The `catalog-service`
gains a degraded-mode fallback: return stale Redis cache results when the primary DB is
unavailable. The Redis cache-hit path MUST be the common case for search (p95 ≤ 100 ms).

**Prerequisite**: `reservation-service` must be migrated from Axon 4.10.3 to Axon 5.x
before the `SpotHold` aggregate and `HoldPricingCoordinatorSaga` can be implemented.

## Technical Context

**Language/Version**: Java 21 (backend) · TypeScript 5.x / Node 20 LTS (frontend)
**Spring Boot Version**: 4.0.6
**Primary Dependencies**: Spring Boot 4.x · Axon Framework **5.x** (migration + new) · Spring Data JPA · Spring Kafka · Resilience4j 2.x · Flyway · springdoc-openapi 2.x · React 18+ · React Query v5 · Testcontainers 1.x · ArchUnit 1.x
**Storage**: PostgreSQL 16 (primary) · Redis 7 (cache-aside + session/availability cache)
**Testing**: JUnit 5 + Mockito (domain unit) · Testcontainers + Spring Boot Test (integration) · ArchUnit (layer enforcement) · Vitest + React Testing Library (frontend)
**Target Platform**: Linux server — Docker / Kubernetes-compatible
**Project Type**: Monorepo — multi-module web application (Spring MVC REST × 4 services + React SPA)
**Performance Goals**: Search p95 ≤ 100 ms (SC-001, cache-hit path is common case) · Hold 202-response p95 ≤ 300 ms (SC-002a) + ACTIVE state within 3 s end-to-end (SC-002b) · SSE latency ≤ 5 s (SC-006)
**Constraints**: Anonymous hold endpoint rate-limited · JWT lifetime ≤ 1 hour · No sticky sessions · Axon 5.x platform-wide (D-005) · No external geocoding API
**Scale/Scope**: New `pricing-service` subproject · 3 updated services (catalog, reservation, frontend) · 3 new Kafka topics · 1 new DB table (spot_hold)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Hexagonal Architecture | `SpotHold` aggregate in `domain/model/` · `HoldController` in `interfaces/rest/` · Saga in `application/saga/` · Kafka adapters in `infrastructure/messaging/` · ArchUnit enforces boundaries | ✅ PASS |
| II. DDD | `SpotHold` is an Axon 5.x `@Aggregate` owning the 6-state machine and double-hold invariant · `HoldId`, `Money`, `ReservationPeriod` as immutable VOs · Domain events raised by aggregate roots | ✅ PASS |
| III. TDD | Tests for `SpotHold` state machine written first · `HoldPricingCoordinatorSaga` unit tests with mocked event bus · `PricingCalculationService` unit tests before implementation · ≥ 90% domain coverage gate in CI · Testcontainers for Kafka + PostgreSQL + Redis integration tests | ✅ PASS |
| IV. Concurrency & Consistency | Axon command serialization prevents double-hold (one command per aggregate at a time) · `@Version` on `spot_hold` table · Unique index `idx_spot_hold_active` on `(spot_id, check_in, check_out)` for active holds · Saga-originated Kafka publishes via Axon tracking event processor (at-least-once delivery) | ✅ PASS |
| V. Performance & Resilience | Search p95 ≤ 100 ms via Redis cache-aside (cache-hit is common case; DB hit is exceptional) · Resilience4j RateLimiter on hold creation · Resilience4j CircuitBreaker on pricing Kafka consumer · Hold TTL via Axon deadline · Load test validates SC-001 and SC-002a/b thresholds as DoD | ✅ PASS |
| VI. Observability | `holdId`, `searchSessionId` added to MDC context · Micrometer counters: `hold.created`, `hold.expired`, `hold.pricing_timeout` · Prometheus alert rules: search p95 > 150 ms, pricing timeout rate > 5%, circuit breaker open | ✅ PASS |
| VII. Language | All new code, events, API contracts, test labels in English | ✅ PASS |

**Result: ALL GATES PASS — design proceeds.**

## Project Structure

### Documentation (this feature)

```text
specs/002-parking-search-hold/
├── plan.md              # This file
├── spec.md              # Feature specification (6-state SpotHold; SC-001=100ms; FR-004/FR-005 in scope)
├── research.md          # Phase 0 output — 8 research items resolved
├── data-model.md        # Phase 1 output — SpotHold aggregate, Kafka schemas, DB migrations
├── quickstart.md        # Phase 1 output — developer setup guide
├── contracts/
│   ├── catalog-api.md   # Search endpoint (pagination, feature filters, searchSessionId, estimatedPrice, SSE)
│   ├── hold-api.md      # Hold creation / polling / release endpoints
│   └── domain-events.md # 6 domain events + 3 Kafka topic schemas
└── tasks.md             # Task breakdown (T001–T056)
```

### Source Code

```text
/ (repository root)
├── settings.gradle                             # ADD: include ':pricing-service'
├── build.gradle                                # Root BOM (no change needed)
│
├── catalog-service/                            # MODIFIED — search enhancements + SSE
│   └── src/main/java/org/labcabrera/parking/catalog/
│       ├── application/
│       │   ├── dto/
│       │   │   ├── SearchRequest.java          # ADD lat, lng, radiusKm, page, size, features fields
│       │   │   ├── SearchResponse.java         # ADD searchSessionId, stale, totalPages, totalElements
│       │   │   └── FacilityResult.java         # ADD estimatedPrice, lowAvailability fields
│       │   └── queries/
│       │       └── SearchParkingQueryHandler   # ADD sessionId gen, estimatedPrice, proximity filter,
│       │                                       #     pagination, feature filter, low-availability flag,
│       │                                       #     degraded Redis fallback
│       └── interfaces/
│           └── rest/
│               ├── CatalogController.java      # ADD GET /availability/stream endpoint
│               └── AvailabilityStreamRegistry  # NEW — SseEmitter registry per facilityId
│
├── reservation-service/                        # MODIFIED — Axon 5.x + SpotHold aggregate
│   ├── build.gradle                            # Axon version: 4.10.3 → 5.x
│   └── src/main/java/org/labcabrera/parking/reservation/
│       ├── domain/
│       │   ├── model/
│       │   │   ├── SpotHold.java               # NEW — Axon 5.x @Aggregate (6-state machine)
│       │   │   └── HoldStatus.java             # NEW — enum (6 states)
│       │   └── port/outbound/
│       │       └── HoldRepository.java         # NEW
│       ├── application/
│       │   ├── commands/                       # NEW — 6 command classes
│       │   └── saga/
│       │       └── HoldPricingCoordinatorSaga  # NEW — Axon Saga
│       ├── infrastructure/
│       │   ├── persistence/
│       │   │   └── SpotHoldJpaEntity.java      # NEW
│       │   ├── messaging/
│       │   │   ├── PricingRequestPublisher.java
│       │   │   ├── PricingResultConsumer.java
│       │   │   └── AvailabilityChangePublisher.java
│       │   └── scheduling/
│       │       └── HoldExpiryScheduler.java    # NEW — fallback sweep job
│       └── interfaces/rest/
│           └── HoldController.java             # NEW — POST/GET/DELETE /holds
│       └── resources/db/migration/
│           └── V2__create_spot_hold.sql        # NEW
│
├── pricing-service/                            # NEW — entire module scaffold
│   ├── build.gradle
│   ├── Dockerfile
│   └── src/main/java/org/labcabrera/parking/pricing/
│       ├── PricingServiceApplication.java
│       ├── domain/service/
│       │   └── PricingCalculationService       # MVP: baseRate × days
│       └── infrastructure/messaging/
│           ├── PricingRequestConsumer.java
│           └── PricingResultPublisher.java
│
├── frontend/                                   # MODIFIED — search form + hold flow
│   └── src/
│       ├── services/
│       │   ├── catalogApi.ts                   # UPDATE response type (searchSessionId, estimatedPrice, pagination)
│       │   └── holdApi.ts                      # NEW
│       ├── hooks/
│       │   ├── useParkingSearch.ts             # UPDATE — expose searchSessionId, pagination
│       │   └── useParkingHold.ts               # NEW — hold creation + polling
│       ├── components/search/
│       │   └── ParkingCard.tsx                 # UPDATE — estimatedPrice, lowAvailability, hold flow
│       └── types/
│           └── hold.ts                         # NEW
│
└── infrastructure/kafka/
    └── init-topics.sh                          # ADD 3 new topics
```

## Complexity Tracking

No constitution violations in this feature. `pricing-service` as a 4th backend service
is justified by D-001 (domain separation). The async two-step hold flow (R-002) is the
minimum viable approach satisfying both FR-010 (confirmed price) and D-001. Pagination
and feature filters (FR-004/FR-005) are confirmed in-scope for this iteration.

---

## Implementation Phases

### Phase A — Prerequisites

#### A-1: Axon 5.x migration — `reservation-service`

- Update `reservation-service/build.gradle`: bump `axon-spring-boot-starter` from
  `4.10.3` to latest `5.x` stable
- Verify existing `ReservationPaymentSaga`: check `@SagaEventHandler` and
  `DeadlineManager` usage for 5.x API compatibility
- Fix any breaking compilation errors
- Run full `reservation-service` test suite
- **Acceptance**: `./gradlew :reservation-service:test` passes with Axon 5.x

#### A-2: Kafka topic provisioning

- Add `parking.pricing.requests`, `parking.pricing.results`,
  `parking.availability.changes` to `infrastructure/kafka/init-topics.sh`
- **Acceptance**: `kafka-topics.sh --list` shows all three topics

---

### Phase B — `catalog-service` Search Enhancements

#### B-1: DTO extensions

- Add `lat`, `lng`, `radiusKm` (optional) to `SearchRequest` (proximity filter)
- Add `page`, `size` (optional, default configurable) to `SearchRequest` — FR-004
- Add `features: List<String>` (optional) to `SearchRequest` — FR-005
- Add `searchSessionId`, `stale`, `totalPages`, `totalElements` to `SearchResponse`
- Add `estimatedPrice: Money`, `lowAvailability: boolean` to `FacilityResult` — FR-006

#### B-2: Query handler enhancements (TDD)

- Generate `searchSessionId` (UUID v4) per request
- Compute `estimatedPrice = dailyRate × days` per facility result
- Apply haversine proximity filter when `lat`+`lng`+`radiusKm` present
- Apply `features` filter when provided — FR-005
- Apply `lowAvailability` flag when available spots < configurable threshold — FR-006
- Apply Spring Data pagination (`Page<T>`) — FR-004; expose `catalog.search.max-page-size`
- Degraded-mode fallback (R-006): `DataAccessException` → Redis cache → `stale: true`;
  503 only if both DB and cache fail

#### B-3: SSE infrastructure

- `AvailabilityStreamRegistry`: `CopyOnWriteArrayList<SseEmitter>` per `facilityId`,
  heartbeat every 30 s via `@Scheduled`
- `GET /api/v1/catalog/availability/stream` `SseEmitter` endpoint
- `AvailabilityChangeConsumer`: Kafka consumer on `parking.availability.changes`;
  pushes SSE event + invalidates Redis cache for affected `facilityId`

---

### Phase C — `pricing-service` Module Scaffold

#### C-1: Gradle subproject setup

- Create `pricing-service/` with hexagonal package structure
- `pricing-service/build.gradle` (Spring Boot 4.0.6, Axon 5.x, Spring Kafka)
- Add `:pricing-service` to root `settings.gradle`
- `Dockerfile` following existing service pattern

#### C-2: Kafka consumer + producer + MVP pricing (TDD — domain layer first)

- **Write failing unit tests** for `PricingCalculationService` before implementation
- `PricingCalculationService`: `confirmedPrice = baseRatePerDay × days`
- `PricingRequestConsumer`: listens on `parking.pricing.requests`
- `PricingResultPublisher`: publishes to `parking.pricing.results`
- Integration test with Testcontainers Kafka

---

### Phase D — `reservation-service` SpotHold Aggregate

#### D-1: `SpotHold` aggregate (TDD — critical path)

- **Write tests first** using `AggregateTestFixture<SpotHold>` — all 6 state
  transitions, double-hold invariant, deadline → `HoldExpiredEvent`
- Implement as Axon 5.x `@Aggregate`: 6 `@CommandHandler` methods,
  6 `@EventSourcingHandler` methods, `@DeadlineHandler("hold-expiry")`,
  double-hold guard on `CreateHoldCommand`
- Auto-release prior PENDING_PRICE/ACTIVE hold by same `searchSessionId` (FR-011)

#### D-2: `HoldPricingCoordinatorSaga` (TDD)

- **Write tests first** using `SagaTestFixture<HoldPricingCoordinatorSaga>`
- Saga started by `HoldCreatedEvent`; publishes Kafka pricing request; schedules
  `pricing-timeout` Axon deadline; on result → `ConfirmHoldPriceCommand`;
  on timeout → `FailHoldPricingCommand`

#### D-3: JPA persistence + Flyway migration

- `SpotHoldJpaEntity`, `HoldJpaRepository`
- `V2__create_spot_hold.sql`: table + unique index `idx_spot_hold_active` + `@Version` column

#### D-4: Messaging adapters

- `PricingRequestPublisher` → Kafka `parking.pricing.requests`
- `PricingResultConsumer` → Axon event bus for saga
- `AvailabilityChangePublisher` → `parking.availability.changes` on `HoldCreated`,
  `HoldExpired`, `HoldReleased`

#### D-5: Rate limiting

- Resilience4j `RateLimiter` beans: `hold-creation-by-ip` (5 req/60 s),
  `hold-creation-by-session` (3 req/60 s)
- Applied via `HandlerInterceptor` in interfaces layer only

#### D-6: `HoldController` REST adapter

- `POST /api/v1/reservations/holds` → `202 Accepted`
- `GET /api/v1/reservations/holds/{holdId}` → `200` / `410 Gone` for terminal states
- `DELETE /api/v1/reservations/holds/{holdId}` → `204` / `409 Conflict` if terminal
- Input validation: UUID `searchSessionId`, `checkOut > checkIn`

#### D-7: `HoldExpiryScheduler`

- `@Scheduled(fixedDelayString="PT5M")` fallback sweep
- Issues `ExpireHoldCommand` for `PENDING_PRICE`/`ACTIVE` holds past `expiresAt`

---

### Phase E — Frontend Updates

#### E-1: Types and API clients

- `frontend/src/types/hold.ts`: `HoldResponse`, `HoldStatus` (6 values)
- `catalogApi.ts`: extended `SearchResponse` (pagination metadata, `searchSessionId`, `stale`)
- `holdApi.ts` (NEW): `createHold()`, `getHold()`, `releaseHold()`

#### E-2: Hooks

- `useParkingSearch`: expose `searchSessionId`, pagination metadata, `stale` banner
- `useParkingHold` (NEW): `createHold()` → polls `getHold()` every 500 ms while
  `PENDING_PRICE`; stops on terminal state; surfaces expiry/error

#### E-3: UI components

- `ParkingCard`: display `estimatedPrice`, `lowAvailability` badge, "Select" button →
  triggers `useParkingHold`; spinner while `PENDING_PRICE`; confirmed price when `ACTIVE`

---

### Phase F — Cross-Cutting Concerns

#### F-1: Observability

- `holdId`, `searchSessionId` to MDC in `HoldController` and `SearchParkingQueryHandler`
- Micrometer counters: `hold.created`, `hold.expired`, `hold.pricing_timeout`,
  `hold.released`, `hold.converted`
- Prometheus alert rules: search p95 > 150 ms, pricing timeout rate > 5%,
  circuit breaker open

#### F-2: ArchUnit

- `SpotHold` has no Spring/JPA imports in domain layer
- `HoldController` imports no domain model classes directly
- `HoldPricingCoordinatorSaga` in `application.saga` package only

#### F-3: API documentation

- `@Operation`, `@ApiResponse`, `@Parameter` on `HoldController` and SSE endpoint

#### F-4: Load test

- Load test `GET /catalog/search` (Redis cache warm): validate SC-001 (p95 ≤ 100 ms)
- Load test `POST /reservations/holds` → poll until `ACTIVE`: validate SC-002b (p95 ≤ 3 s)
- Task is DoD — merge blocked until both thresholds pass

---

## Key Design Decisions

| Decision | Choice | Reference |
|----------|--------|-----------|
| Axon version | 5.x platform-wide | D-005, R-001 |
| Confirmed price delivery | Two-step 202 → `PENDING_PRICE` → `ACTIVE` via Kafka saga | R-002 |
| Hold TTL management | Axon `DeadlineManager` + fallback `@Scheduled` sweep | R-003 |
| Rate limiting | Resilience4j `RateLimiter` in-process (interfaces layer) | R-004 |
| SSE implementation | Spring MVC `SseEmitter` | R-005 |
| Degraded search | Redis cache-hit is common case; `stale` fallback on DB failure | R-006 |
| `searchSessionId` persistence | UUID v4 in-memory, no DB table this iteration | R-007 |
| `pricing-service` scope | MVP scaffold with `baseRate × days`; richer logic deferred | R-008 |
| SC-001 target | 100 ms p95 (aligned with constitution §V; cache-hit path mandatory) | Q1 clarification |
| SC-002 measurement | 202 response p95 ≤ 300 ms (SC-002a) + ACTIVE within 3 s (SC-002b) | Q2 clarification |
| SpotHold state machine | 6 canonical states including `PENDING_PRICE` and `FAILED` | Q3 clarification |
| FR-004/FR-005 scope | Pagination and feature filters both in scope this iteration | Q4 clarification |

## Out of Scope (this iteration)

- Full reservation creation (checkout flow) — spec-003
- Payment processing — spec-003
- User profile management
- Per-facility advance-notice rules
- External geocoding API
- Redis-backed distributed rate limiter (in-process only)
- PostGIS proximity queries (haversine in-application)
- `SearchSession` persistence / analytics
- Pricing algorithm enrichment beyond `baseRate × days`
