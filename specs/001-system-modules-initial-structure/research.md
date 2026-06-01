# Research: System Modules Definition & Initial Structure

**Phase**: 0 — Resolve all unknowns before Phase 1 design
**Date**: 2026-06-01
**Feature**: [spec.md](spec.md) | [plan.md](plan.md)

---

## R-001 · Spring Boot 4.x + Axon Framework Compatibility

**Decision**: Use Axon Framework 4.10+ with the official `axon-spring-boot-starter`. If
the Spring Boot 4.0.6 starter is not yet released at implementation time, pin to the
latest Axon 4.x release that declares Spring Boot 4 compatibility; monitor the Axon GitHub
milestone tracker and update the root `build.gradle` once confirmed.

**Rationale**: Axon Framework follows Spring Boot's release cadence closely and provides
first-class Spring Boot auto-configuration. Spring Boot 4.x requires Spring Framework 7.x
(Java 21 baseline), which Axon 4.10+ targets. Axon's command bus, event bus, and
deadline manager are all Spring-bean-managed, so no bespoke wiring is needed beyond the
starter dependency and `axon.axonserver.enabled=false` (using embedded command bus for
this monorepo deployment).

**Alternatives considered**:
- *Event Sourcing (Axon + AxonServer)*: Rejected. The spec explicitly mandates AXON
  without event sourcing; aggregates persist state via JPA repositories, not an event
  store. AxonServer adds operational overhead that is out of scope for the MVP.
- *MediatR-style command bus without Axon*: Rejected. Axon provides built-in SAGA support
  (tracking sagas, deadline management) that would require significant bespoke
  implementation otherwise.

---

## R-008 · API Documentation — SpringDoc Code-First

**Decision**: Every Spring Boot service MUST include `springdoc-openapi-starter-webmvc-ui`
(version aligned with Spring Boot 4.x). API specifications are generated at runtime from
Java annotations (`@Operation`, `@ApiResponse`, `@Parameter`, `@Schema`). No hand-written
OpenAPI YAML files are maintained.

**Endpoints exposed per service**:
- `GET /v3/api-docs` — machine-readable OpenAPI 3.x JSON
- `GET /swagger-ui.html` — interactive Swagger UI

**Rationale**: Code-first keeps the API contract in sync with the implementation by
construction; there is no drift between a separate YAML file and the actual controller.
SpringDoc integrates with Spring Security so protected endpoints are correctly annotated
with their security requirements.

**Alternatives considered**:
- *Hand-written OpenAPI YAML (design-first)*: Rejected. Adds a manual sync obligation;
  the team is small and the spec is the source of truth rather than a separate YAML.
- *Springfox*: Rejected. Springfox is no longer actively maintained and does not support
  Spring Boot 3+/4+.

---

## R-009 · Build Tool — Gradle Multi-Project with gradlew Wrapper

**Decision**: Use Gradle with **Groovy DSL** (`*.gradle`) for all backend Java modules.
The monorepo root contains `settings.gradle` (listing all subprojects) and `build.gradle`
(shared version catalog / platform BOM). Each subproject has its own `build.gradle`. The
`gradlew` and `gradlew.bat` wrapper scripts plus `gradle/wrapper/gradle-wrapper.jar` and
`gradle-wrapper.properties` MUST be committed to the repository. All services MUST
declare `org.springframework.boot` version `4.0.6` and set `sourceCompatibility = '21'`.

**Rationale**: Gradle's incremental build and build-cache support reduce CI build times
compared to Maven for large multi-module projects. The `gradlew` wrapper ensures every
developer and CI runner uses exactly the same Gradle version without a local installation
requirement. Groovy DSL is chosen over Kotlin DSL per explicit project constraint
(FR-034).

**Alternatives considered**:
- *Maven*: Rejected per spec (FR-034). Replaced by Gradle.
- *Kotlin DSL (`*.gradle.kts`)*: Rejected per explicit project constraint — Groovy DSL
  is mandated.

---

## R-002 · SAGA Pattern with Axon Tracking Saga + Deadline Manager

**Decision**: Implement `ReservationPaymentSaga` as an Axon `@Saga` (tracking saga). The
saga associates on `ReservationCreatedEvent` and registers an Axon `DeadlineManager`
deadline keyed to `price-lock-minutes`. On `PaymentApprovedEvent` the saga confirms the
reservation and cancels the deadline. On deadline expiry it publishes a
`PriceLockExpiredCommand` that drives the `Reservation` aggregate to `EXPIRED`.

**Rationale**: Axon's tracking saga is persistent (state stored in the configured event/
saga store, backed by JPA here), survives service restarts, and integrates natively with
the `DeadlineManager` for timeout handling. This eliminates the need for an external
scheduler (e.g., Quartz) to handle price-lock expiry.

**Compensating transactions defined**:

| SAGA Step | Happy Path Event | Failure / Timeout | Compensating Transaction |
|-----------|-----------------|-------------------|--------------------------|
| 1. Create reservation | `ReservationCreatedEvent` | Pricing call fails | Dispatch `CancelReservationCommand` (state → REJECTED) |
| 2. Lock price | `PriceLockAcquiredEvent` | Deadline fires | Dispatch `PriceLockExpiredCommand` (state → EXPIRED, release spot) |
| 3. Initiate payment | `PaymentInitiatedEvent` | `PaymentDeclinedEvent` | Dispatch `CancelReservationCommand` (state → REJECTED, release spot) |
| 4. Confirm reservation | `PaymentApprovedEvent` | — | — |

---

## R-003 · Transactional Outbox — Polling Publisher

**Decision**: Implement the Outbox pattern using a dedicated `outbox_events` table in each
service's PostgreSQL database. A scheduled `OutboxPoller` (Spring `@Scheduled`, configurable
interval, default 500 ms) reads unpublished rows, publishes to Kafka, and marks them
published within the same JDBC transaction.

**Rationale**: The polling publisher is operationally simpler than CDC (Debezium) for an
MVP: no additional Kafka Connect cluster is required, and the Outbox table schema and
polling interval are fully under the service's control. The 500 ms polling interval is
acceptable given the asynchronous nature of the payment SAGA; latency-critical paths
(search, availability queries) do not use the Outbox.

**Alternatives considered**:
- *Debezium CDC*: Rejected for MVP. Adds Kafka Connect and schema registry dependencies.
  Documented as a future upgrade path in `docs/adr/001-technology-choices.md`.
- *Direct Kafka publish in transaction*: Rejected. Kafka transactions and DB transactions
  cannot participate in a single XA transaction without a transaction manager; dual-write
  creates data loss risk.

**Outbox schema** (per service):
```sql
CREATE TABLE outbox_events (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id VARCHAR(255) NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      JSONB        NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at TIMESTAMPTZ
);
CREATE INDEX idx_outbox_unpublished ON outbox_events (published_at) WHERE published_at IS NULL;
```

---

## R-004 · Redis Cache-Aside for Availability Queries

**Decision**: The `CatalogService` availability query path uses a cache-aside pattern:
on a search request, check Redis first (key: `availability:{facilityId}:{date}`); on miss,
query PostgreSQL and write to Redis with a TTL of 60 seconds (configurable). Cache entries
are explicitly invalidated when a `ReservationConfirmedEvent` or `SpotReleasedEvent` is
consumed by the Catalog Service's Kafka consumer.

**Rationale**: Cache-aside gives full control over cache population and invalidation. A
60-second TTL caps stale data exposure. Explicit invalidation on reservation events ensures
the low-availability warning reflects reality within one event-processing cycle (typically
< 1 second under normal load), well within the acceptable freshness window for a parking
booking UX.

**Key schema**:
- Search results: `catalog:search:{hashOf(query+dates)}` · TTL: 30 s (search results are
  less stable; shorter TTL acceptable)
- Facility availability: `catalog:availability:{facilityId}:{checkIn}:{checkOut}` · TTL:
  60 s

**Invalidation triggers**:
- `ReservationConfirmedEvent` → invalidate `catalog:availability:{facilityId}:*`
- `SpotReleasedEvent` (from cancellation or EXPIRED) → same

---

## R-005 · Keycloak Server-Side User Registration via Admin REST API

**Decision**: The `reservation-service` exposes `POST /api/v1/auth/register`. The
`KeycloakRegistrationAdapter` in the infrastructure layer calls the Keycloak Admin REST
API (`POST /admin/realms/{realm}/users`) using a dedicated service-account client
(`parking-backend-client` with `manage-users` role) whose credentials are injected via
environment variables. On success the adapter obtains a user token via the password grant
(immediately after creation) and returns a short-lived JWT to the frontend.

**Security controls**:
- Keycloak admin client secret is environment-variable-injected; never committed to the
  repository.
- The registration endpoint is rate-limited (Resilience4j `RateLimiter`, 10 req/min per
  IP) to prevent account enumeration.
- Email uniqueness is enforced by Keycloak; duplicate registration returns a 409 Conflict.

**Alternatives considered**:
- *Keycloak self-registration UI redirect*: Rejected per spec clarification (Q3). Breaks
  the inline flow required by the UX specification.

---

## R-006 · Real-Time Availability Updates — Server-Sent Events (SSE)

**Decision**: The Catalog Service exposes a `GET /api/v1/catalog/availability/stream`
SSE endpoint. The frontend subscribes on the search results page using the browser's
native `EventSource` API. The backend pushes `availability-update` events whenever the
cached availability for a displayed facility changes.

**Rationale**: SSE is unidirectional (server → client), which matches the use case
exactly. It requires no additional broker or WebSocket infrastructure; Spring MVC supports
it natively via `SseEmitter`. Connections are stateless on the server side (emitters stored
in an in-memory map keyed to `facilityId`; harmless to lose on restart since clients
reconnect automatically).

**Alternatives considered**:
- *WebSocket*: Rejected. Bidirectional communication is not needed for availability
  updates; adds complexity for no benefit.
- *Client-side polling*: Rejected. Does not meet FR-029 ("without requiring a full page
  reload") efficiently at scale; creates unnecessary load.

---

## R-007 · Search Full-Text Strategy — PostgreSQL `pg_trgm` + `tsvector`

**Decision**: Use PostgreSQL's `pg_trgm` extension for fuzzy free-text matching on
facility names and `tsvector` / `tsquery` for keyword search on tags and city names. A
composite GIN index on `(name, city, tags)` supports both patterns. Geolocation bounding
box filtering uses a `POINT` column with a GiST index.

**Rationale**: For the MVP, PostgreSQL full-text search avoids adding Elasticsearch as an
operational dependency. The Redis cache-aside strategy (R-004) absorbs repeated identical
queries. At the scale of the full production system, migrating to Elasticsearch is a
documented future option (see `docs/adr/001-technology-choices.md`).

**Index DDL**:
```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
ALTER TABLE parking_facilities ADD COLUMN search_vector TSVECTOR
    GENERATED ALWAYS AS (
        to_tsvector('english', coalesce(name,'') || ' ' || coalesce(city,'') || ' ' || coalesce(tags_text,''))
    ) STORED;
CREATE INDEX idx_facilities_search_gin ON parking_facilities USING GIN (search_vector);
CREATE INDEX idx_facilities_name_trgm  ON parking_facilities USING GIN (name gin_trgm_ops);
CREATE INDEX idx_facilities_location   ON parking_facilities USING GIST (location);
```

**Alternatives considered**:
- *Elasticsearch*: Documented as the upgrade path for production-scale full-text search;
  out of scope for initial structure MVP.
- *ILIKE with `%text%`*: Rejected. Sequential scan; does not scale beyond thousands of
  rows.
