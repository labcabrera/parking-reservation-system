---
description: "Task list for Parking Search & Hold Flow (Shopping Session)"
---

# Tasks: Parking Search & Hold Flow (Shopping Session)

**Feature Branch**: `002-parking-search-hold`
**Input**: [spec.md](spec.md) · [plan.md](plan.md) · [data-model.md](data-model.md) · [contracts/](contracts/) · [research.md](research.md) · [quickstart.md](quickstart.md)
**Tests**: TDD tasks are included **only** for the `SpotHold` aggregate and `HoldPricingCoordinatorSaga`, as explicitly required by the plan. All other test tasks are optional.

## Format: `[ID] [P?] [Story] Description — file path`

- **[P]**: Can run in parallel (independent files, no dependencies on incomplete tasks)
- **[US1/US2/US3]**: User story assignment
- Setup and Foundational phases have no story label

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Axon 5.x upgrade, new `pricing-service` module scaffold, Kafka topic provisioning.
This phase blocks all reservation-service work (US2, US3). catalog-service (US1 search part) can start in parallel.

- [ ] T001 Update `axon-spring-boot-starter` from `4.10.3` to `5.x` stable in `reservation-service/build.gradle`
- [ ] T002 [P] Add `:pricing-service` to `settings.gradle`
- [ ] T003 [P] Create `pricing-service/build.gradle` with Spring Boot 4.0.6, Axon 5.x, Spring Kafka, springdoc-openapi, and Testcontainers dependencies
- [ ] T004 [P] Create `pricing-service/Dockerfile` following `catalog-service/Dockerfile` pattern
- [ ] T005 [P] Create `pricing-service/src/main/java/org/labcabrera/parking/pricing/PricingServiceApplication.java` and empty hexagonal package structure (`domain/`, `application/`, `infrastructure/`, `interfaces/`)
- [ ] T006 [P] Add `parking.pricing.requests`, `parking.pricing.results`, `parking.availability.changes` to `infrastructure/kafka/init-topics.sh`
- [ ] T007 Run `./gradlew :reservation-service:test` to validate Axon 5.x compatibility and fix any breaking API changes in existing sagas or event handlers

**Checkpoint**: `reservation-service` compiles and all existing tests pass with Axon 5.x. `pricing-service` builds. Kafka topics are provisioned.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Database migration, JPA entity, ports, and command/event classes shared across US2 and US3.
**⚠️ CRITICAL**: No hold-related user story work (US2, US3) can begin until this phase is complete.

- [ ] T008 Create Flyway migration `reservation-service/src/main/resources/db/migration/V2__create_spot_hold.sql` — `spot_hold` table, unique index `idx_spot_hold_active` on `(spot_id, check_in, check_out) WHERE status IN ('PENDING_PRICE', 'ACTIVE')`, and `@Version` column, per `data-model.md`
- [ ] T009 [P] Create `HoldStatus.java` enum (`PENDING_PRICE`, `ACTIVE`, `EXPIRED`, `RELEASED`, `FAILED`, `CONVERTED`) in `reservation-service/src/main/java/org/labcabrera/parking/reservation/domain/model/HoldStatus.java`
- [ ] T010 [P] Create `SpotHoldJpaEntity.java` in `reservation-service/src/main/java/org/labcabrera/parking/reservation/infrastructure/persistence/SpotHoldJpaEntity.java` mapping all fields from `data-model.md` to the `spot_hold` table
- [ ] T011 [P] Create `HoldRepository.java` port interface in `reservation-service/src/main/java/org/labcabrera/parking/reservation/domain/port/outbound/HoldRepository.java`
- [ ] T012 [P] Create all 6 Hold command classes in `reservation-service/src/main/java/org/labcabrera/parking/reservation/application/commands/`: `CreateHoldCommand`, `ConfirmHoldPriceCommand`, `FailHoldPricingCommand`, `ReleaseHoldCommand`, `ExpireHoldCommand`, `ConvertHoldCommand`
- [ ] T013 [P] Create all 6 Hold domain event classes in `reservation-service/src/main/java/org/labcabrera/parking/reservation/domain/model/events/`: `HoldCreated`, `HoldPriceConfirmed`, `HoldPricingFailed`, `HoldReleased`, `HoldExpired`, `HoldConverted`

**Checkpoint**: Foundation ready — US2 and US3 implementation can now begin.

---

## Phase 3: User Story 1 — Visitor searches for available parking (Priority: P1) 🎯 MVP

**Goal**: Extend `catalog-service` search to return `searchSessionId`, `estimatedPrice`, and `stale` flag; add proximity filter; add SSE availability stream. Extend frontend to display estimated price and expose `searchSessionId`.

**Independent Test**: Submit `GET /api/v1/catalog/search?query=...&checkIn=...&checkOut=...`. Response contains `searchSessionId` (UUID v4), each facility includes `estimatedPrice`, and `stale: false` under normal conditions. Connect `GET /api/v1/catalog/availability/stream?facilityId=...` and verify heartbeat comment arrives within 35 s.

### Implementation — catalog-service DTOs

- [ ] T014 [P] [US1] Add `lat`, `lng`, `radiusKm` (optional) fields to `catalog-service/src/main/java/org/labcabrera/parking/catalog/application/dto/SearchRequest.java`
- [ ] T015 [P] [US1] Add `searchSessionId` (String/UUID) and `stale` (boolean) fields to `catalog-service/src/main/java/org/labcabrera/parking/catalog/application/dto/SearchResponse.java`
- [ ] T016 [P] [US1] Add `estimatedPrice` (`Money` value object: amount + currency) field to `catalog-service/src/main/java/org/labcabrera/parking/catalog/application/dto/FacilityResult.java`

### Implementation — catalog-service query handler

- [ ] T017 [US1] Update `catalog-service/src/main/java/org/labcabrera/parking/catalog/application/queries/SearchParkingQueryHandler.java`: generate UUID v4 `searchSessionId`, compute `estimatedPrice = dailyRate × days`, apply haversine proximity filter when `lat`/`lng`/`radiusKm` are present
- [ ] T018 [US1] Add Redis cache-aside degraded-mode fallback to `SearchParkingQueryHandler`: on `DataAccessException` attempt Redis lookup; set `stale: true` in response; return 503 only if both DB and cache are unavailable

### Implementation — catalog-service SSE stream

- [ ] T019 [P] [US1] Create `catalog-service/src/main/java/org/labcabrera/parking/catalog/interfaces/rest/AvailabilityStreamRegistry.java` — `CopyOnWriteArrayList<SseEmitter>` per `facilityId`, emitter cleanup on `onCompletion`/`onTimeout`/`onError`, heartbeat every 30 s via `@Scheduled`
- [ ] T020 [US1] Add `GET /api/v1/catalog/availability/stream` `SseEmitter` endpoint to `catalog-service/src/main/java/org/labcabrera/parking/catalog/interfaces/rest/CatalogController.java` — validates `facilityId`, registers in `AvailabilityStreamRegistry`
- [ ] T021 [US1] Create `catalog-service/src/main/java/org/labcabrera/parking/catalog/infrastructure/messaging/AvailabilityChangeConsumer.java` — Kafka consumer for `parking.availability.changes`; on message, push `availability-update` SSE event via `AvailabilityStreamRegistry` and invalidate Redis cache for affected `facilityId`

### Implementation — frontend

- [ ] T022 [P] [US1] Update `frontend/src/types/` with extended search types: `SearchResponse` (add `searchSessionId`, `stale`), `FacilityResult` (add `estimatedPrice`)
- [ ] T023 [P] [US1] Update `frontend/src/services/catalogApi.ts` `searchParking()` to return extended response type including `searchSessionId` and `estimatedPrice` per result
- [ ] T024 [US1] Update `frontend/src/hooks/useParkingSearch.ts` to expose `searchSessionId` from the search response for use by downstream hold creation
- [ ] T025 [US1] Update `frontend/src/components/search/ParkingCard.tsx` to display `estimatedPrice` per facility result (FR-002)

**Checkpoint**: Search returns `searchSessionId`, `estimatedPrice`, proximity filtering works, SSE stream emits heartbeats and availability events. Frontend displays estimated price.

---

## Phase 4: User Story 2 — Visitor selects a parking and obtains a confirmed price hold (Priority: P2)

**Goal**: Implement the full two-step hold flow: `POST /holds → 202 PENDING_PRICE → Kafka pricing round-trip → ACTIVE` with confirmed price. Includes `pricing-service` MVP, `SpotHold` aggregate, `HoldPricingCoordinatorSaga`, rate limiting, hold REST API, and frontend hold flow.

**Independent Test**: Using `searchSessionId` from a search, `POST /api/v1/reservations/holds`. Response is `202 Accepted` with `status: PENDING_PRICE`. Poll `GET /holds/{holdId}` until `status: ACTIVE` and `confirmedPrice` is populated. `DELETE /holds/{holdId}` returns `204`. Submitting 6th request within 60 s from the same IP returns `429`.

### Implementation — pricing-service MVP

- [ ] T026 [US2] Create `pricing-service/src/main/java/org/labcabrera/parking/pricing/domain/service/PricingCalculationService.java` — MVP formula: `confirmedPrice = baseRatePerDay × days`. **(depends on T055 — TDD: tests must exist and fail before this task)**
- [ ] T027 [US2] Create `pricing-service/src/main/java/org/labcabrera/parking/pricing/infrastructure/messaging/PricingRequestConsumer.java` — Kafka consumer on `parking.pricing.requests` (group-id: `pricing-service`); delegates to `PricingCalculationService`
- [ ] T028 [US2] Create `pricing-service/src/main/java/org/labcabrera/parking/pricing/infrastructure/messaging/PricingResultPublisher.java` — Kafka producer to `parking.pricing.results`

### Implementation — SpotHold aggregate (TDD)

- [ ] T029 [US2] Write `AggregateTestFixture<SpotHold>` unit tests in `reservation-service/src/test/java/org/labcabrera/parking/reservation/domain/model/SpotHoldTest.java` covering: all 6 state transitions, double-hold rejection, deadline firing → `HoldExpiredEvent`, and FR-023 (new `CreateHoldCommand` succeeds when previous hold is in terminal `FAILED` state). **TDD: confirm tests FAIL before implementing T030.**
- [ ] T030 [US2] Implement `reservation-service/src/main/java/org/labcabrera/parking/reservation/domain/model/SpotHold.java` as Axon 5.x `@Aggregate` with full 6-state machine, all 6 `@CommandHandler` methods, all 6 `@EventSourcingHandler` methods, `@DeadlineHandler("hold-expiry")` for TTL, and double-hold invariant guard. All tests from T029 must pass.

### Implementation — HoldPricingCoordinatorSaga (TDD)

- [ ] T031 [US2] Write `SagaTestFixture<HoldPricingCoordinatorSaga>` tests in `reservation-service/src/test/java/org/labcabrera/parking/reservation/application/saga/HoldPricingCoordinatorSagaTest.java` covering: saga start on `HoldCreated`, pricing result → `ConfirmHoldPriceCommand`, deadline timeout → `FailHoldPricingCommand`. **TDD: confirm tests FAIL before T032.**
- [ ] T032 [US2] Implement `reservation-service/src/main/java/org/labcabrera/parking/reservation/application/saga/HoldPricingCoordinatorSaga.java` — Axon `@Saga` started by `HoldCreatedEvent`; publishes `PricingRequest` to Kafka via outbound port; schedules `pricing-timeout` deadline using `reservation.hold.pricing-timeout-seconds` (default 30 s, wired from `application.yml`); on `PricingResultReceived` cancels deadline and sends `ConfirmHoldPriceCommand`; on deadline sends `FailHoldPricingCommand`. All tests from T031 must pass.

### Implementation — reservation-service messaging adapters

- [ ] T033 [P] [US2] Create `reservation-service/src/main/java/org/labcabrera/parking/reservation/infrastructure/messaging/PricingRequestPublisher.java` — Kafka producer to `parking.pricing.requests` (called by saga)
- [ ] T034 [P] [US2] Create `reservation-service/src/main/java/org/labcabrera/parking/reservation/infrastructure/messaging/PricingResultConsumer.java` — Kafka consumer on `parking.pricing.results`; dispatches `PricingResultReceivedEvent` to Axon event bus for saga pickup
- [ ] T035 [P] [US2] Create `reservation-service/src/main/java/org/labcabrera/parking/reservation/infrastructure/messaging/AvailabilityChangePublisher.java` — Kafka producer to `parking.availability.changes`; triggered by `HoldCreatedEvent` and `HoldReleasedEvent`. Note: `HoldExpiredEvent` subscription is handled in T045 (US3) to avoid split between phases; T035 must expose a generic `publish(facilityId, availableSpots)` method reusable by both

### Implementation — rate limiting and REST adapter

- [ ] T036 [US2] Configure Resilience4j `RateLimiter` beans in `reservation-service` application configuration: `hold-creation-by-ip` (5 req/60 s per IP) and `hold-creation-by-session` (3 req/60 s per `searchSessionId`); apply to hold creation endpoint
- [ ] T037 [US2] Create `reservation-service/src/main/java/org/labcabrera/parking/reservation/interfaces/rest/HoldController.java` with: `POST /api/v1/reservations/holds` → `202 Accepted`; `GET /api/v1/reservations/holds/{holdId}` → `200` (any state) or `404 Not Found` for unknown/purged IDs (per FR-022); `DELETE /api/v1/reservations/holds/{holdId}` → `204` or `409 Conflict` for terminal states

### Implementation — frontend hold flow

- [ ] T038 [P] [US2] Create `frontend/src/types/hold.ts` with `HoldResponse` interface and `HoldStatus` type (`PENDING_PRICE | ACTIVE | EXPIRED | RELEASED | FAILED | CONVERTED`)
- [ ] T039 [P] [US2] Create `frontend/src/services/holdApi.ts` with `createHold()`, `getHold()`, `releaseHold()` functions calling the reservation-service hold endpoints
- [ ] T040 [US2] Create `frontend/src/hooks/useParkingHold.ts` — calls `createHold()`, then polls `getHold()` every 500 ms while `status === 'PENDING_PRICE'`; returns `{ hold, isLoading, isError }`
- [ ] T041 [US2] Update `frontend/src/pages/SearchPage.tsx` and `frontend/src/components/search/ParkingCard.tsx` to trigger `useParkingHold` on facility selection, show spinner while `PENDING_PRICE`, and display confirmed price when `ACTIVE`

**Checkpoint**: Full hold flow works end-to-end: search → select → 202 PENDING → pricing Kafka round-trip → ACTIVE with confirmedPrice. Rate limit returns 429 on excess requests.

---

## Phase 5: User Story 3 — Hold expires automatically if visitor does not proceed (Priority: P3)

**Goal**: Axon `DeadlineManager` inside `SpotHold` handles TTL expiry. A `@Scheduled` sweep job serves as fallback. Frontend surfaces expiry to the user.

**Independent Test**: Create a hold. Set hold TTL to 1 minute (or trigger deadline manually in test). Wait for TTL to elapse. Verify `GET /holds/{holdId}` returns `status: EXPIRED` and `410 Gone` for further DELETE/confirm attempts. Verify `parking.availability.changes` event was published.

- [ ] T042 [US3] Create `reservation-service/src/main/java/org/labcabrera/parking/reservation/infrastructure/scheduling/HoldExpiryScheduler.java` — `@Scheduled(fixedDelayString="PT5M")` fallback sweep that issues `ExpireHoldCommand` for all `PENDING_PRICE` or `ACTIVE` holds whose `expiresAt` is in the past (guards against Axon deadline delivery failures)
- [ ] T043 [US3] Verify `HoldController` returns `404 Not Found` for `GET /holds/{holdId}` when the hold ID is unknown or has been purged (per FR-022), and `409 Conflict` for `DELETE /holds/{holdId}` when the hold is in a terminal state (`EXPIRED`, `RELEASED`, `CONVERTED`, `FAILED`) — add/fix response handling in `reservation-service/.../interfaces/rest/HoldController.java`
- [ ] T044 [US3] Update `frontend/src/hooks/useParkingHold.ts` — stop polling when `status` is `EXPIRED` or `FAILED`; surface an expiry/error message to the caller so the UI can prompt the user to restart the search
- [ ] T045 [US3] Verify `AvailabilityChangePublisher.java` in `reservation-service` publishes `parking.availability.changes` on `HoldExpiredEvent` (add `@EventHandler` subscription if not already covered by T035) so SSE clients receive availability updates when a hold expires

**Checkpoint**: Expired holds are reflected as EXPIRED in API responses, spot is returned to available inventory, and frontend informs the user to start a new selection.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Observability, ArchUnit enforcement, and API documentation across all modified services.

- [ ] T046 [P] Add `holdId` and `searchSessionId` to MDC context in `reservation-service/.../interfaces/rest/HoldController.java` and `catalog-service/.../application/queries/SearchParkingQueryHandler.java`
- [ ] T047 [P] Add Micrometer counters in `reservation-service` domain event handlers: `hold.created`, `hold.expired`, `hold.pricing_timeout`, `hold.released`, `hold.converted` — expose via `/actuator/prometheus`
- [ ] T048 [P] Add ArchUnit rules in `reservation-service/src/test/` verifying: `SpotHold` has no Spring/JPA imports in domain layer; `HoldController` imports no domain model classes directly (only port interfaces); `HoldPricingCoordinatorSaga` resides in `application.saga` package only
- [ ] T049 [P] Add `@Operation`, `@ApiResponse`, `@Parameter` springdoc-openapi annotations to `reservation-service/.../interfaces/rest/HoldController.java` and to the SSE endpoint in `catalog-service/.../interfaces/rest/CatalogController.java`
- [ ] T050 Update `README.md` for `reservation-service/README.md`, `catalog-service/README.md`, and `pricing-service/README.md` with hold flow configuration properties, Kafka topic setup, and quickstart commands from `quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
  - T001–T007 can start in parallel except T007 depends on T001
- **Foundational (Phase 2)**: Requires Phase 1 completion — **BLOCKS US2 and US3**
  - T008–T013 are independent of each other (all [P])
- **User Story 1 (Phase 3)**: Requires only T006 (Kafka topics) for SSE consumer (T021); otherwise independent — can start immediately after T001
  - T014, T015, T016, T019, T022, T023 are fully independent [P]
  - T017 depends on T014, T015, T016
  - T018 depends on T017
  - T020 depends on T019
  - T021 depends on T006, T019, T020
- **User Story 2 (Phase 4)**: Requires Phase 1 + Phase 2 completion
  - T026, T027, T028 (pricing-service) depend on T002–T005
  - T029 must precede T030 (TDD)
  - T031 must precede T032 (TDD)
  - T033, T034, T035 are independent [P] after Phase 2
  - T036 precedes T037
  - T037 depends on T030, T032, T033, T034, T035, T036
  - T038, T039 are independent [P]
  - T040 depends on T038, T039
  - T041 depends on T024, T040
- **User Story 3 (Phase 5)**: Requires T030 (SpotHold aggregate) and T037 (HoldController)
  - T044, T045 depend on T040 (useParkingHold hook)
- **Polish (Phase 6)**: Requires all user story phases complete

### User Story Independence

- **US1 (P1)**: Independently deployable — only touches `catalog-service` and `frontend/search`
- **US2 (P2)**: Depends on US1 `searchSessionId`; independently testable via API (no frontend required)
- **US3 (P3)**: Depends on US2 `SpotHold` aggregate; the scheduler and deadline handler are isolated additions

---

## Parallel Execution Examples

### Phase 1 — Parallel setup

```text
Start together:
  T002 [P] — Add :pricing-service to settings.gradle
  T003 [P] — Create pricing-service/build.gradle
  T004 [P] — Create pricing-service/Dockerfile
  T005 [P] — Create PricingServiceApplication.java
  T006 [P] — Add Kafka topics to init-topics.sh

Then:
  T001     — Update reservation-service Axon version (needed for T007)
  T007     — Run reservation-service tests (after T001)
```

### Phase 3 (US1) — Parallel DTO and frontend work

```text
Start together (fully independent):
  T014 [P] [US1] — Extend SearchRequest DTO
  T015 [P] [US1] — Extend SearchResponse DTO
  T016 [P] [US1] — Extend FacilityResult DTO
  T019 [P] [US1] — Create AvailabilityStreamRegistry
  T022 [P] [US1] — Update frontend search types
  T023 [P] [US1] — Update catalogApi.ts

Then (depends on above):
  T017 [US1]     — Update SearchParkingQueryHandler (needs T014, T015, T016)
  T020 [US1]     — Add SSE endpoint (needs T019)
  T024 [US1]     — Update useParkingSearch hook (needs T023)
  T025 [US1]     — Update ParkingCard (needs T022)

Then:
  T018 [US1]     — Add degraded-mode fallback (needs T017)
  T021 [US1]     — Create AvailabilityChangeConsumer (needs T020)
```

### Phase 4 (US2) — Parallel messaging adapters and frontend

```text
Start together after Phase 2:
  T026 [P] [US2] — PricingCalculationService
  T033 [P] [US2] — PricingRequestPublisher
  T034 [P] [US2] — PricingResultConsumer
  T035 [P] [US2] — AvailabilityChangePublisher
  T038 [P] [US2] — frontend/src/types/hold.ts
  T039 [P] [US2] — frontend/src/services/holdApi.ts

TDD sequence (serial):
  T029 [US2]     — Write SpotHold tests (then FAIL check)
  T030 [US2]     — Implement SpotHold aggregate
  T031 [US2]     — Write Saga tests (then FAIL check)
  T032 [US2]     — Implement HoldPricingCoordinatorSaga
  T036 [US2]     — Configure Resilience4j
  T037 [US2]     — Create HoldController (needs T030, T032, T033–T036)

Frontend sequence (serial, can overlap with backend):
  T040 [US2]     — useParkingHold hook (needs T038, T039)
  T041 [US2]     — SearchPage + ParkingCard hold integration
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Complete Phase 1 — Setup
2. Complete Phase 2 — Foundational
3. Complete Phase 3 — US1 (search enhancements + SSE)
4. **STOP and VALIDATE**: Search returns `searchSessionId` and `estimatedPrice`, SSE stream works
5. Demo or deploy US1 increment

### Incremental Delivery

1. Setup + Foundational → infrastructure ready
2. **US1** → searchable catalog with session tracking and SSE (MVP!)
3. **US2** → confirmed-price hold flow end-to-end
4. **US3** → auto-expiry and inventory recovery
5. **Polish** → observability, ArchUnit, docs

### Single-Developer Sequence

```
T001 → T002 → T003 → T004 → T005 → T006 → T007
→ T008 → T009 → T010 → T011 → T012 → T013
→ T014 → T015 → T016 → T017 → T018 → T019 → T020 → T021
→ T022 → T023 → T024 → T025
→ T026 → T027 → T028 → T029 → T030 → T031 → T032
→ T033 → T034 → T035 → T036 → T037
→ T038 → T039 → T040 → T041
→ T042 → T043 → T044 → T045
→ T046 → T047 → T048 → T049 → T050
→ T051 → T052 → T053 → T054
→ T056
```

---

## Phase 7: Additions from Clarification (FR-004 / FR-005 / FR-006 / TDD / Load Test)

**Purpose**: Tasks added after the speckit.clarify session that confirmed pagination (FR-004), feature filters (FR-005), and low-availability warning (FR-006) are in scope for this iteration; also TDD prerequisite for pricing and load-test DoD gate.

### catalog-service — Pagination (FR-004)

- [ ] T051 [US1] Extend `SearchRequest.java` with `page` (int, default 0) and `size` (int, default from config) fields; extend `SearchResponse.java` with `totalPages` (int) and `totalElements` (long) fields; add `catalog.search.max-page-size` property to `catalog-service/src/main/resources/application.yml`; update `SearchParkingQueryHandler` to use Spring Data `Pageable` and populate pagination metadata in `SearchResponse`. **Depends on T014, T015, T017.**

### catalog-service — Feature Filters (FR-005)

- [ ] T052 [US1] Add `features: List<String>` field to `SearchRequest.java`; apply multi-value AND predicate in `SearchParkingQueryHandler` (only return facilities offering all requested features); add `INVALID_FEATURE_VALUE` input validation for unknown feature names. Accepted values: `COVERED`, `EV_CHARGING`, `FREE_CANCELLATION`, `WHEELCHAIR_ACCESSIBLE`, `VALET`. **Depends on T014, T017.**

### catalog-service — Low-Availability Warning (FR-006)

- [ ] T053 [US1] Add `lowAvailability: boolean` field to `FacilityResult.java`; add `catalog.search.low-availability-threshold` config property (default: 5); apply threshold check in `SearchParkingQueryHandler` and set `lowAvailability = true` when `availableSpots < threshold`. **Depends on T016, T017.**

### Frontend — Extended Search Types

- [ ] T054 [P] [US1] Update `frontend/src/types/` search types to include pagination fields (`page`, `size`, `totalPages`, `totalElements`) in `SearchResponse`; add `lowAvailability: boolean` to `FacilityResult`; add `features?: string[]` to `SearchRequest` type; update `catalogApi.ts` and `useParkingSearch.ts` to expose pagination metadata; update `ParkingCard.tsx` to display `lowAvailability` badge. **Depends on T022, T023, T024, T025.**

### TDD prerequisite — pricing-service (before T026)

- [ ] T055 [US2] Write failing unit tests for `PricingCalculationService` in `pricing-service/src/test/java/org/labcabrera/parking/pricing/domain/service/PricingCalculationServiceTest.java` covering: `confirmedPrice = baseRatePerDay × days` with edge cases (same-day, multi-week, zero rate). **TDD: confirm tests FAIL before implementing T026. Depends on T002–T005.**

### Load test — DoD gate for SC-001 and SC-002b

- [ ] T056 Load test `GET /api/v1/catalog/search` with warm Redis cache using k6 or Gatling from `catalog-service/src/test/load/`; assert p95 ≤ 100 ms (SC-001). Load test `POST /api/v1/reservations/holds` + poll until `ACTIVE`; assert p95 time-to-ACTIVE ≤ 3 s (SC-002b). **Merge to `develop` is blocked until both thresholds pass. Depends on T017, T018, T037.**

**Checkpoint**: Pagination works on search results; feature filters correctly narrow results; low-availability badge appears when spots are below threshold; frontend shows all new fields; PricingCalculationService has full test coverage; load test passes SC-001 and SC-002b.

---

## Phase 8: Post-Clarify Additions (FR-021 / FR-022 / FR-023 / OTEL / SC-003 / ArchUnit)

**Purpose**: Tasks added after speckit.clarify round 2 and speckit.analyze remediation pass. Covers idempotency guard (FR-021), pricing-timeout config wiring (H3), OTEL instrumentation (§VI), SC-003 integration test, and catalog-service ArchUnit enforcement (§I).

### Hold endpoint — Idempotency guard (FR-021)

- [ ] T057 [US2] Implement idempotency guard in `reservation-service/.../interfaces/rest/HoldController.java` (or a pre-dispatch application service): before issuing `CreateHoldCommand`, query `HoldRepository` for an existing hold with matching `searchSessionId` + `facilityId` + period in `PENDING_PRICE` or `ACTIVE` state. If found, return `200 OK` with the existing hold response. If not found, proceed to dispatch command and return `202 Accepted`. **Depends on T011 (HoldRepository), T037 (HoldController).**

### Pricing-timeout configuration wiring (H3)

- [ ] T057b Add `reservation.hold.pricing-timeout-seconds=30` to `reservation-service/src/main/resources/application.yml`; inject value into `HoldPricingCoordinatorSaga` via `@Value`; use it as the `DeadlineManager` deadline duration. Add corresponding test in `HoldPricingCoordinatorSagaTest` asserting deadline fires at the configured duration. **Depends on T031, T032.**

### Observability — OTEL span instrumentation (§VI, M4)

- [ ] T058 [P] Add OTEL span instrumentation to the pricing round-trip: annotate `HoldPricingCoordinatorSaga` `@SagaEventHandler` methods with Micrometer `@Observed` (or manual `Tracer.startScopedSpan`); add trace propagation header to the `PricingRequest` Kafka message; add span to `PricingResultConsumer` when dispatching `PricingResultReceivedEvent`. **Depends on T032, T034.**

### SC-003 TTL integration test (M3)

- [ ] T059 Write Testcontainers integration test in `reservation-service/src/test/java/.../SpotHoldTtlIntegrationTest.java`: create a hold with a 10-second TTL override; assert `GET /holds/{holdId}` returns `status: EXPIRED` within 30 seconds of TTL elapse (SC-003). **Depends on T030, T037, T042.**

### catalog-service ArchUnit enforcement (§I, M2)

- [ ] T060 [P] Add ArchUnit rules in `catalog-service/src/test/java/.../CatalogArchitectureTest.java` verifying: `SearchParkingQueryHandler` has no infrastructure imports; `CatalogController` imports no domain model classes directly; `AvailabilityChangeConsumer` resides in `infrastructure.messaging` package; `AvailabilityStreamRegistry` resides in `interfaces.rest` or `infrastructure` package. **Depends on T021.**

**Checkpoint**: Idempotency guard prevents duplicate holds on retry; pricing-timeout is configurable and tested; OTEL spans trace the full pricing round-trip; SC-003 integration test proves TTL expiry within 30 s; catalog-service architecture boundaries are enforced in CI.

---

## Updated Dependencies

- T051 depends on T014, T015, T017
- T052 depends on T014, T017
- T053 depends on T016, T017
- T054 depends on T022, T023, T024, T025 (can be done in parallel with T051–T053)
- T055 must precede T026 (TDD prerequisite)
- T056 depends on T017, T018, T037 (last DoD gate before Phase 8)
- T057 depends on T011, T037
- T057b depends on T031, T032
- T058 depends on T032, T034
- T059 depends on T030, T037, T042
- T060 depends on T021
