# Research: Parking Search & Hold Flow

**Phase**: 0 — Research
**Date**: 2026-06-01
**Feature**: [spec.md](spec.md) | [plan.md](plan.md)

---

## R-001: Axon Framework — Migration from 4.10.x to 5.x

### Context
The `reservation-service` currently depends on `axon-spring-boot-starter:4.10.3`.
The `catalog-service` does not use Axon. Decision D-005 mandates Axon 5.x
platform-wide: `reservation-service` migrates; `pricing-service` and any new hold
aggregate are implemented directly in 5.x.

### Decision
Migrate `reservation-service` to Axon 5.x. Implement `SpotHold` aggregate and
`pricing-service` directly in Axon 5.x. Do **not** introduce Axon into `catalog-service`
for this feature.

### Key Breaking Changes (4.10 → 5.x)
| Area | 4.x | 5.x |
|------|-----|-----|
| BOM artifact | `axon-spring-boot-starter` | `axon-spring-boot-starter` (same, version bump) |
| Package root | `org.axonframework.*` | `org.axonframework.*` (unchanged) |
| Aggregate annotation | `@Aggregate` + `@AggregateIdentifier` | Same |
| `CommandGateway` | `send()` returns `CompletableFuture` | Reactor-aware variant added; `send()` still works |
| Saga `@SagaEventHandler` | Requires `associationProperty` | Same; verify `SagaConfiguration` autowiring changed |
| `DeadlineManager` | Injected into aggregate constructor | Injected via `@Autowired` or constructor in 5.x (verify) |
| `@DeadlineHandler` | Annotation-based | Same — verify method signature requirements |
| Event store | Default in-memory; production needs AxonServer or JDBC | Same |
| Kotlin coroutine support | Limited | Expanded in 5.x |

### Rationale
Axon 5.x is the current major version at plan date. Using a single version across the
platform prevents dependency conflicts and allows sharing serialization configuration.

### Alternatives Considered
- Stay on 4.10.x → rejected (D-005 decision; constitution mandates unified tech stack)
- Use Spring state machine instead of Axon aggregates → rejected (breaks DDD aggregate
  pattern enforced by constitution)

### Migration Tasks
- Update `build.gradle` in `reservation-service` to Axon 5.x coordinates
- Verify `ReservationPaymentSaga` still compiles (check `@SagaEventHandler` and
  `DeadlineManager` usage)
- Run ArchUnit tests after migration to confirm no layer violations

---

## R-002: Hold Creation — Synchronous vs Asynchronous Price Confirmation

### Context
FR-010 requires that a confirmed price is returned in the hold creation response. D-001
mandates Kafka for pricing-service integration. These two requirements create a tension:
the client expects a synchronous response but the pricing channel is asynchronous.

### Decision
Use a **two-step flow** with Axon saga coordination:

1. `POST /reservations/holds` → creates hold in `PENDING_PRICE` state → returns
   `202 Accepted` with `holdId` and `status: PENDING_PRICE`.
2. Reservation-service saga publishes `PricingRequestEvent` to Kafka topic
   `parking.pricing.requests`.
3. `pricing-service` consumes the event, calculates confirmed price, publishes
   `PricingResultEvent` to `parking.pricing.results`.
4. Reservation-service Kafka consumer receives result → saga transitions hold to `ACTIVE`
   with `confirmedPrice` populated.
5. Client polls `GET /reservations/holds/{holdId}` until status is `ACTIVE` or `FAILED`.

**TTL countdown starts at step 1** (PENDING_PRICE), not at step 4.

Hold state machine (full):
```
PENDING_PRICE → ACTIVE (pricing result received)
              → FAILED (pricing-service error or timeout)
ACTIVE        → EXPIRED (TTL elapsed)
             → RELEASED (new selection in same session)
             → CONVERTED (visitor advances to reservation)
```

### Rationale
- Keeps the Kafka-first design for pricing (D-001)
- Avoids synchronous coupling between reservation-service and pricing-service
- p95 ≤ 3s (SC-002) is achievable: Kafka round-trip within same local cluster is
  typically < 200 ms; pricing calculation is lightweight
- Frontend can show a "confirming price…" spinner; most users won't notice the gap

### Alternatives Considered
- `ReplyingKafkaTemplate` (sync request-reply on Kafka) → increases coupling, harder to
  test; rejected
- Direct REST call from reservation-service to pricing-service → violates D-001 decision
  and introduces direct service coupling; rejected
- Return estimated price immediately and update asynchronously → violates FR-010
  ("confirmed price returned with hold"); rejected

### Notes for Implementation
- `HoldPricingCoordinatorSaga` tracks `holdId` ↔ `pricingCorrelationId`
- Saga sets an Axon deadline equal to `pricing.confirmation.timeout-seconds` (default 10 s);
  if deadline fires before result → hold transitions to `FAILED`
- Polling endpoint MUST use `ETag` / `Last-Modified` to avoid thundering-herd

---

## R-003: SpotHold TTL Management

### Context
FR-009 / FR-012 require that holds expire automatically. Two strategies are viable:
1. Axon `DeadlineManager` (deadline scheduled inside aggregate)
2. External scheduler (Spring `@Scheduled` + database TTL query)

### Decision
Use **Axon `DeadlineManager`** inside the `SpotHold` aggregate.

```java
// Inside SpotHold aggregate
@DeadlineHandler(deadlineName = "hold-expiry")
void onHoldExpiry() {
    // raise HoldExpiredEvent — spot returned to inventory
    apply(new HoldExpiredEvent(this.holdId));
}
```

The deadline is scheduled immediately after the `HoldCreatedEvent` is applied, using
`DeadlineManager.schedule(Duration.ofMinutes(ttlMinutes), "hold-expiry")`.

### Rationale
- Natural fit for Axon aggregates — deadline lives with the aggregate lifecycle
- No external polling loop or job scheduler to maintain
- Deadline survives restarts when AxonServer or JDBC event store is used (persistent
  deadline)
- Centralises TTL logic inside the aggregate boundary

### Alternatives Considered
- Spring `@Scheduled` job scanning DB for expired holds → adds polling overhead, increases
  DB load, harder to unit test in isolation; rejected for primary mechanism (acceptable
  as a safety fallback sweep job)
- Database TTL column + trigger → non-portable, harder to intercept in application layer;
  rejected

### Notes for Implementation
- `reservation.hold.ttl-minutes` (default: 10) must be injected into aggregate via
  command message or application config passed at command handling time
- A fallback sweep job (`@Scheduled(fixedDelay = "PT5M")`) SHOULD be added to catch any
  missed deadlines (Axon deadline delivery is at-least-once in clustered environments)

---

## R-004: Rate Limiting on Anonymous Endpoints

### Context
FR-017 requires rate limiting on hold creation per IP and per `searchSessionId`. The
service stack does not include an API Gateway with built-in rate limiting.

### Decision
Use **Resilience4j `RateLimiter`** at the service layer (within `reservation-service`).

Two rate limiters:
- `hold-creation-by-ip`: max 5 requests / 60 s per `X-Forwarded-For` IP
- `hold-creation-by-session`: max 3 requests / 60 s per `searchSessionId`

Both limits are configurable via `application.yml`:
```yaml
resilience4j.ratelimiter:
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

### Rationale
- Resilience4j is already a dependency in the service
- No additional infrastructure component needed (no API Gateway or Redis-based
  distributed limiter required for this iteration)
- Configurable per-environment

### Alternatives Considered
- Spring Cloud Gateway rate limiter (Redis-backed) → requires Gateway service; overkill
  for current scale; deferred to future horizontal scaling concern; rejected for now
- Custom `HandlerInterceptor` with in-memory `ConcurrentHashMap<String, AtomicInteger>`
  → doesn't survive restart, doesn't work in clustered deployment; rejected

### Limitation
Resilience4j `RateLimiter` is in-process → does not enforce limits across multiple
instances. Acceptable for this iteration (single instance per service). Document as
known limitation; plan to replace with Redis-backed limiter when horizontal scaling is
required.

---

## R-005: SSE (Server-Sent Events) for Availability Stream

### Context
FR-015 requires a real-time availability stream endpoint. D-006 scopes SSE to
`GET /catalog/availability/stream` only. The catalog-service uses Spring MVC (not
WebFlux).

### Decision
Implement SSE using **Spring MVC `SseEmitter`**.

```java
@GetMapping(value = "/availability/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter availabilityStream(@RequestParam String facilityId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    // register emitter in AvailabilityStreamRegistry
    // on SseEmitter.onCompletion / onTimeout / onError → deregister
    return emitter;
}
```

`AvailabilityStreamRegistry` holds a `CopyOnWriteArrayList<SseEmitter>` per `facilityId`.
When a `SpotAvailabilityChangedEvent` is received from Kafka (published by
`reservation-service` after hold/release), the registry pushes the update to all
registered emitters.

Heartbeat: emit a comment event every 30 s to keep the connection alive across proxies.

### Rationale
- Spring MVC `SseEmitter` is production-ready for moderate connection counts
- No framework change required (WebFlux adds complexity)
- Kafka consumption → push to SSE clients provides clean decoupling

### Alternatives Considered
- WebFlux `Flux<ServerSentEvent>` → requires switching catalog-service to reactive stack;
  significant refactor; deferred; rejected for this iteration
- WebSocket → bi-directional, heavier protocol; not needed (server-push only); rejected
- Polling by frontend → already covered by standard search; SSE is additive; accepted as
  complementary

### Notes for Implementation
- `SseEmitter` is not thread-safe by default; wrap push operations in `synchronized` or
  use `ConcurrentHashMap`
- Emitters must be removed on `onCompletion`, `onTimeout`, and `onError` callbacks to
  prevent memory leaks
- In clustered deployments, Kafka consumers on each node receive all availability events
  and push to locally-registered clients — no cross-node SSE routing needed for this
  iteration

---

## R-006: Redis Cache-Aside with Degraded Mode

### Context
FR-020 requires that search returns `stale: true` results when the DB is unavailable.
SC-004 requires 99% success under healthy conditions. The cache-aside pattern is already
in use via `RedisAvailabilityCache`.

### Decision
Extend the search query handler with a **degraded-mode fallback**:

```
1. Try DB query → success → populate cache → return results (stale: false)
2. DB unavailable → try Redis cache → hit → return results (stale: true)
3. DB unavailable + cache miss / Redis unavailable → return 503
```

The `SearchParkingQueryHandler` uses a try-catch around the DB call; if a
`DataAccessException` is thrown, it falls back to `AvailabilityCache.getSearchResults()`.

### Rationale
- Minimal change to existing cache adapter — add `getSearchResults()` method
- Wraps fallback logic in the application layer query handler (not in the domain layer)
- Keeps infrastructure concern (DB/Redis failure) at the adapter boundary

### Notes for Implementation
- Cache key for search: `search:{queryHash}` where `queryHash = SHA-256(location + checkIn
  + checkOut + filters)` (first 16 hex chars for readability)
- Cache TTL: configurable `catalog.cache.search.ttl-seconds` (default: 300 s)
- `SearchResponse` DTO gains a `boolean stale` field
- ArchUnit test must verify `DataAccessException` handling is in infrastructure/application
  layer and NOT in domain layer

---

## R-007: `searchSessionId` Generation and Lifecycle

### Context
FR-003 requires a unique `searchSessionId` returned with each search. FR-007 / FR-011
use it to correlate holds. The session ID must be generated by the backend.

### Decision
Generate `searchSessionId` as a **UUID v4** at search execution time in the query handler.
Do **not** persist the search session to the database in this iteration; the
`searchSessionId` is opaque to the client and is validated at hold-creation time by
verifying it is a well-formed UUID.

Hold-creation validates that the received `searchSessionId` is a valid UUID; no lookup
against a search session table is performed. The `SearchSession` entity described in the
spec is a logical concept; its persistence is deferred to a future iteration when session
analytics are required.

### Rationale
- Simplest viable approach: no new table migration, no state to expire
- Still uniquely identifies the session for rate limiting (R-004) and automatic hold
  release (FR-011) — both keyed on `searchSessionId`
- Persistent session analytics is a future concern (not required by any SC in this spec)

### Alternatives Considered
- Persist `SearchSession` to PostgreSQL → adds a Flyway migration, adds latency on hot
  path; deferred; rejected for this iteration
- JWT-signed session token → verifiable without DB lookup, adds crypto complexity;
  overkill for current requirements; rejected

---

## R-008: `pricing-service` Subproject Scaffold

### Context
D-001 establishes `pricing-service` as a new Gradle subproject. This feature scaffolds
the module; full pricing logic is out of scope here (covered in a future spec).

### Decision
Add `pricing-service` to `settings.gradle`. The module scaffold includes:
- `build.gradle` with Spring Boot, Axon 5.x, Spring Kafka dependencies
- Main application class `PricingServiceApplication`
- Hexagonal package structure: `domain/`, `application/`, `infrastructure/`, `interfaces/`
- Kafka consumer listening on `parking.pricing.requests`
- Kafka producer publishing to `parking.pricing.results`
- MVP pricing algorithm: `baseRatePerDay × numberOfDays` (same as estimated price
  for now — to be replaced by a richer algorithm in a future spec)
- Dockerfile following existing pattern

### Rationale
- Creates the infrastructure for Kafka-based pricing without blocking the hold flow
- Enables testing of the full async pricing round-trip in this iteration
- Pricing algorithm enrichment is deferred without blocking the feature

---

## Summary Table

| Research Item | Decision | Impact |
|---------------|----------|--------|
| R-001 | Migrate reservation-service to Axon 5.x; pricing-service on 5.x | Prerequisite task |
| R-002 | Two-step hold flow (202 → PENDING_PRICE → ACTIVE via Kafka/saga) | New hold state added |
| R-003 | Axon `DeadlineManager` for TTL | No Spring Scheduler for main path |
| R-004 | Resilience4j `RateLimiter` (in-process) | Known cluster limitation |
| R-005 | Spring MVC `SseEmitter` + Kafka push | Catalog-service stays Spring MVC |
| R-006 | Degraded cache fallback + `stale: true` in SearchResponse | New DTO field |
| R-007 | UUID v4 for searchSessionId, no DB persistence this iteration | No new table |
| R-008 | pricing-service scaffold with Kafka + MVP algorithm | New Gradle subproject |
