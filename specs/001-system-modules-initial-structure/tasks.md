---
description: "Task list for System Modules Definition & Initial Structure"
---

# Tasks: System Modules Definition & Initial Structure

**Input**: Design documents from `/specs/001-system-modules-initial-structure/`
**Prerequisites**: plan.md ✅  spec.md ✅  research.md ✅  data-model.md ✅  contracts/ ✅  quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing:
- **US1 (P1)**: Developer bootstraps the full system locally with `docker compose up`
- **US2 (P2)**: Architect reviews module boundaries and integration contracts
- **US3 (P3)**: Customer searches for available parking without logging in

**Tests**: No TDD tasks included (not requested in spec.md). ArchUnit layer-enforcement tests ARE included as they are a hard requirement (FR-002, SC-007).

---

## Phase 1: Setup (Monorepo Bootstrap)

**Purpose**: Initialize the Gradle multi-project build, directory skeleton, and frontend project shells. This creates the repo structure that everything else depends on.

- [ ] T001 Create Gradle wrapper files at repo root: `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.x), `gradlew`, `gradlew.bat`
- [ ] T002 Create root `settings.gradle` (Groovy DSL) declaring `rootProject.name = 'parking-reservation-system'` and including subprojects: `catalog-service`, `reservation-service`, `payment-service`
- [ ] T003 Create root `build.gradle` (Groovy DSL) with `plugins` block (spring-boot 4.0.6, dependency-management), `allprojects`/`subprojects` configuration, Java 21 toolchain, and Spring Boot BOM import
- [ ] T004 [P] Create `catalog-service/build.gradle` applying `java`, `org.springframework.boot`, `io.spring.dependency-management`; declare dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-data-redis, spring-kafka, axon-spring-boot-starter, flyway-core, springdoc-openapi-starter-webmvc-ui, resilience4j-spring-boot3, archunit-junit5, spring-boot-starter-test, testcontainers (postgresql, redis, kafka)
- [ ] T005 [P] Create `reservation-service/build.gradle` with same core dependencies as T004 plus axon-messaging-api; add spring-boot-starter-security and spring-security-oauth2-resource-server for JWT validation
- [ ] T006 [P] Create `payment-service/build.gradle` with spring-boot-starter-web, spring-boot-starter-data-jpa, spring-kafka, axon-spring-boot-starter, flyway-core, springdoc-openapi-starter-webmvc-ui, resilience4j-spring-boot3, archunit-junit5, spring-boot-starter-test, testcontainers
- [ ] T007 [P] Scaffold `frontend/` Vite+React project: create `frontend/package.json` (react 18, react-dom, react-router-dom v6, @tanstack/react-query v5, oidc-client-ts, typescript 5.x, @vitejs/plugin-react), `frontend/vite.config.ts`, `frontend/tsconfig.json`, `frontend/index.html`
- [ ] T008 [P] Scaffold `payment-gateway/` Vite+React project: create `payment-gateway/package.json` (react 18, react-dom, react-router-dom v6, typescript 5.x, @vitejs/plugin-react), `payment-gateway/vite.config.ts`, `payment-gateway/tsconfig.json`, `payment-gateway/index.html`
- [ ] T009 [P] Create `docs/adr/001-technology-choices.md` documenting: Java 21 + Spring Boot 4.0.6 rationale, Axon Framework 4.10+ (commands + SAGA, no event sourcing), Gradle Groovy DSL, hexagonal architecture, React 18/Vite, Keycloak 24+ OIDC, Transactional Outbox pattern decision

---

## Phase 2: Foundational (Service Skeletons — Blocks All User Stories)

**Purpose**: Each Spring Boot service must compile and start with an empty 4-layer package structure before any user story work begins. ArchUnit tests require these packages to exist.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Catalog Service Skeleton

- [ ] T010 Create Spring Boot main class `CatalogServiceApplication.java` at `catalog-service/src/main/java/org/labcabrera/parking/catalog/CatalogServiceApplication.java` annotated with `@SpringBootApplication`
- [ ] T011 [P] Create package-info.java (or `.gitkeep`) files establishing the 4-layer structure under `catalog-service/src/main/java/org/labcabrera/parking/catalog/`: `domain/model/`, `domain/port/inbound/`, `domain/port/outbound/`, `domain/service/`, `application/queries/`, `application/dto/`, `infrastructure/persistence/`, `infrastructure/cache/`, `infrastructure/config/`, `interfaces/rest/`
- [ ] T012 [P] Create `catalog-service/src/main/resources/application.yml` with: `server.port=8081`, `spring.datasource` (PostgreSQL catalog_db), `spring.data.redis` connection, `spring.kafka.bootstrap-servers`, `spring.flyway.enabled=true`, `management.endpoints.web.exposure.include=health,info,metrics`, `springdoc.api-docs.path=/v3/api-docs`

### Reservation Service Skeleton

- [ ] T013 Create Spring Boot main class `ReservationServiceApplication.java` at `reservation-service/src/main/java/org/labcabrera/parking/reservation/ReservationServiceApplication.java` annotated with `@SpringBootApplication`
- [ ] T014 [P] Create package-info.java files establishing the 4-layer structure under `reservation-service/src/main/java/org/labcabrera/parking/reservation/`: `domain/model/`, `domain/port/inbound/`, `domain/port/outbound/`, `domain/service/`, `application/commands/`, `application/queries/`, `application/saga/`, `application/dto/`, `infrastructure/persistence/`, `infrastructure/messaging/`, `infrastructure/external/`, `infrastructure/keycloak/`, `infrastructure/config/`, `interfaces/rest/`, `interfaces/messaging/`
- [ ] T015 [P] Create `reservation-service/src/main/resources/application.yml` with: `server.port=8082`, `spring.datasource` (reservation_db), `spring.kafka.bootstrap-servers`, `spring.security.oauth2.resourceserver.jwt.issuer-uri` (Keycloak realm URL), `axon.axonserver.enabled=false`, `management.endpoints.web.exposure.include=health,info,metrics`, `springdoc.api-docs.path=/v3/api-docs`

### Payment Service Skeleton

- [ ] T016 Create Spring Boot main class `PaymentServiceApplication.java` at `payment-service/src/main/java/org/labcabrera/parking/payment/PaymentServiceApplication.java` annotated with `@SpringBootApplication`
- [ ] T017 [P] Create package-info.java files establishing the 4-layer structure under `payment-service/src/main/java/org/labcabrera/parking/payment/`: `domain/model/`, `domain/port/inbound/`, `domain/port/outbound/`, `application/commands/`, `application/queries/`, `application/dto/`, `infrastructure/persistence/`, `infrastructure/messaging/`, `infrastructure/config/`, `interfaces/rest/`, `interfaces/messaging/`
- [ ] T018 [P] Create `payment-service/src/main/resources/application.yml` with: `server.port=8083`, `spring.datasource` (payment_db), `spring.kafka.bootstrap-servers`, `axon.axonserver.enabled=false`, `management.endpoints.web.exposure.include=health,info,metrics`, `springdoc.api-docs.path=/v3/api-docs`

### Flyway Migration Stubs

- [ ] T019 [P] Create `catalog-service/src/main/resources/db/migration/V1__create_catalog_schema.sql` with DDL for `parking_facility` (id UUID PK, name, city, address, latitude, longitude, total_spots, status, version BIGINT) and `parking_spot` (id UUID PK, facility_id UUID FK, spot_number, type, availability_status, version BIGINT)
- [ ] T020 [P] Create `reservation-service/src/main/resources/db/migration/V1__create_reservation_schema.sql` with DDL for `reservation` (id UUID PK, spot_id, facility_id, user_id, check_in, check_out, status, idempotency_key, version BIGINT), `user_profile` (keycloak_subject, full_name_enc, email_enc, vehicle_plate_enc, requires_invoice, terms_accepted_at), and `outbox_events` (id UUID PK, aggregate_type, aggregate_id, event_type, payload JSONB, created_at, published_at)
- [ ] T021 [P] Create `payment-service/src/main/resources/db/migration/V1__create_payment_schema.sql` with DDL for `payment` (id UUID PK, reservation_id, method, amount, currency, status, external_reference, idempotency_key, version BIGINT)

**Checkpoint**: All three services compile with `./gradlew :catalog-service:compileJava :reservation-service:compileJava :payment-service:compileJava`. All three start with `./gradlew :catalog-service:bootRun` (with local infra).

---

## Phase 3: User Story 1 — Developer Bootstraps the Full System (Priority: P1) 🎯 MVP

**Goal**: `docker compose up` from repo root starts all containers (PostgreSQL, Redis, Kafka, Keycloak, 3 Spring Boot services, 2 frontends) and all health checks pass within 5 minutes.

**Independent Test**: `docker compose up -d && sleep 60 && curl -f http://localhost:8081/actuator/health && curl -f http://localhost:8082/actuator/health && curl -f http://localhost:8083/actuator/health` — all return `{"status":"UP"}`. Keycloak admin at `http://localhost:8080`. Frontends load at `http://localhost:3000` and `http://localhost:3001`.

### Implementation for User Story 1

- [ ] T022 [US1] Create `docker-compose.yml` at repo root with services: `postgres` (postgres:16-alpine, 3 databases via init.sql, healthcheck), `redis` (redis:7-alpine, healthcheck), `zookeeper` (confluentinc/cp-zookeeper:7.6, healthcheck), `kafka` (confluentinc/cp-kafka:7.6, depends_on zookeeper, init script via command, healthcheck), `keycloak` (quay.io/keycloak/keycloak:24, import realm-export.json, healthcheck `/health/ready`), `catalog-service` (build: ./catalog-service, port 8081, depends_on postgres+redis+kafka+keycloak, healthcheck), `reservation-service` (build: ./reservation-service, port 8082, depends_on postgres+kafka+keycloak), `payment-service` (build: ./payment-service, port 8083, depends_on postgres+kafka), `frontend` (build: ./frontend, port 3000), `payment-gateway` (build: ./payment-gateway, port 3001)
- [ ] T023 [P] [US1] Create `catalog-service/Dockerfile` multi-stage build: stage 1 uses `gradle:8-jdk21` to run `./gradlew :catalog-service:bootJar`, stage 2 uses `eclipse-temurin:21-jre-alpine` copying the jar with `ENTRYPOINT ["java", "-jar", "/app.jar"]`
- [ ] T024 [P] [US1] Create `reservation-service/Dockerfile` with same multi-stage pattern as T023
- [ ] T025 [P] [US1] Create `payment-service/Dockerfile` with same multi-stage pattern as T023
- [ ] T026 [P] [US1] Create `frontend/Dockerfile` multi-stage: stage 1 uses `node:20-alpine` to run `npm ci && npm run build`, stage 2 uses `nginx:alpine` copying `dist/` to `/usr/share/nginx/html/`; include `frontend/nginx.conf` with `try_files` for SPA routing and proxy pass for `/api/`
- [ ] T027 [P] [US1] Create `payment-gateway/Dockerfile` with same multi-stage pattern as T026; include `payment-gateway/nginx.conf`
- [ ] T028 [P] [US1] Add `spring-boot-starter-actuator` to each service `build.gradle` and verify `management.endpoints.web.exposure.include=health,info,metrics` in each `application.yml`; confirm `GET /actuator/health` returns `{"status":"UP"}` on startup
- [ ] T029 [P] [US1] Create `infrastructure/keycloak/realm-export.json` with: realm `parking-realm`, client `parking-client` (confidential, standard flow, bearer-only), realm roles `CUSTOMER` and `ADMINISTRATOR`, test user `testuser@parking.local` / `test1234` with `CUSTOMER` role assigned
- [ ] T030 [P] [US1] Create `infrastructure/kafka/init-topics.sh` using `kafka-topics.sh` to create: `parking.reservations` (3 partitions, replication-factor 1), `parking.payments.requests` (3 partitions, replication-factor 1), `parking.payments.results` (3 partitions, replication-factor 1); make the script idempotent (use `--if-not-exists`)
- [ ] T031 [P] [US1] Create `infrastructure/postgres/init.sql` with: `CREATE DATABASE catalog_db;`, `CREATE DATABASE reservation_db;`, `CREATE DATABASE payment_db;` (wrapped in DO $$ blocks to handle idempotent runs)
- [ ] T032 [US1] Create minimal `frontend/src/main.tsx` (ReactDOM.createRoot → `<App />`), `frontend/src/App.tsx` (BrowserRouter with single route `/` → placeholder `<div>Parking Reservation System</div>`); add `frontend/.env.example` with `VITE_CATALOG_SERVICE_URL=http://localhost:8081` and `VITE_PAYMENT_GATEWAY_URL=http://localhost:3001`; verify `npm run dev` serves on port 3000
- [ ] T033 [US1] Create minimal `payment-gateway/src/main.tsx`, `payment-gateway/src/App.tsx` (BrowserRouter with single route `/payment` → placeholder `<div>Payment Gateway</div>`); add `payment-gateway/.env.example` with `VITE_PAYMENT_SERVICE_URL=http://localhost:8083`; verify `npm run dev` serves on port 3001

**Checkpoint**: `docker compose up` brings all containers to healthy state. Each backend service responds to `/actuator/health`. Keycloak console accessible. Both frontends load without console errors.

---

## Phase 4: User Story 2 — Architect Reviews Module Boundaries (Priority: P2)

**Goal**: Any developer or architect can verify bounded context separation and hexagonal layer structure by running ArchUnit tests and reading the ADRs, READMEs, and C4 diagrams — without reading implementation code.

**Independent Test**: `./gradlew :catalog-service:test --tests "*.ArchitectureTest" :reservation-service:test --tests "*.ArchitectureTest" :payment-service:test --tests "*.ArchitectureTest"` — all 3 test classes pass with 0 failures. `docs/architecture/c4-context.md` and `c4-containers.md` exist. Each service has a `README.md`.

### Implementation for User Story 2

- [ ] T034 [US2] Create `ArchitectureTest.java` at `catalog-service/src/test/java/org/labcabrera/parking/catalog/ArchitectureTest.java` using ArchUnit 1.x JUnit5 extension; enforce rules: (1) `domain` has no dependency on `application`, `infrastructure`, or `interfaces`; (2) `application` has no dependency on `infrastructure` or `interfaces`; (3) `infrastructure` and `interfaces` may depend on `application` and `domain`; (4) `interfaces` must not depend on `infrastructure` directly
- [ ] T035 [P] [US2] Create `ArchitectureTest.java` at `reservation-service/src/test/java/org/labcabrera/parking/reservation/ArchitectureTest.java` with the same 4 layer rules applied to the `org.labcabrera.parking.reservation` package tree
- [ ] T036 [P] [US2] Create `ArchitectureTest.java` at `payment-service/src/test/java/org/labcabrera/parking/payment/ArchitectureTest.java` with the same 4 layer rules applied to the `org.labcabrera.parking.payment` package tree
- [ ] T037 [P] [US2] Create `catalog-service/README.md` documenting: bounded context (Catalog & Availability), inbound ports (`SearchParkingPort`), outbound ports (`FacilityRepository`, `AvailabilityCache`), REST endpoints summary (`GET /api/v1/catalog/search`), Kafka events consumed (reservation events for cache invalidation), local run instructions
- [ ] T038 [P] [US2] Create `reservation-service/README.md` documenting: bounded context (Reservations & SAGA), Axon `@Aggregate` and `@Saga` usage, inbound ports, outbound ports, command/query summary, SAGA orchestration flow (ReservationPaymentSaga → PriceLock deadline), Kafka topics produced/consumed, local run instructions
- [ ] T039 [P] [US2] Create `payment-service/README.md` documenting: bounded context (Payment Simulation), Axon `@Aggregate` usage, inbound/outbound ports, command/query summary, Kafka topics consumed (`parking.payments.requests`) and produced (`parking.payments.results`), redirect flow with payment-gateway SPA, local run instructions
- [ ] T040 [P] [US2] Create `docs/architecture/c4-context.md` with a Mermaid diagram showing: external actor "Customer" (browser), external actor "Administrator" (browser), "Parking Reservation System" (system box), external "Keycloak" IdP; label synchronous and asynchronous interaction arrows
- [ ] T041 [P] [US2] Create `docs/architecture/c4-containers.md` with a Mermaid diagram showing all containers: catalog-service (port 8081), reservation-service (port 8082), payment-service (port 8083), frontend SPA (port 3000), payment-gateway SPA (port 3001), PostgreSQL×3 (catalog_db, reservation_db, payment_db), Redis 7, Apache Kafka (3 topics), Keycloak 24; label each connector with protocol (REST, Kafka, JDBC, Redis, OIDC)

**Checkpoint**: All 3 ArchUnit tests pass. All 3 service READMEs created. Both C4 diagrams created.

---

## Phase 5: User Story 3 — Customer Searches Available Parking (Priority: P3)

**Goal**: An unauthenticated user submits a free-text search from the frontend and receives a list of matching parking facilities with availability info for the requested period.

**Independent Test**: `curl "http://localhost:8081/api/v1/catalog/search?q=madrid&checkIn=2026-07-01T10:00&checkOut=2026-07-03T18:00"` returns HTTP 200 with a JSON array containing at least 1 facility from seed data. Frontend `SearchPage` at `http://localhost:3000` renders results.

### Catalog Domain (US3)

- [ ] T042 [P] [US3] Create value object records in `catalog-service/src/main/java/org/labcabrera/parking/catalog/domain/model/`: `FacilityId.java` (record wrapping UUID, static `of(UUID)` factory), `SpotId.java` (same pattern), `Coordinates.java` (record: double latitude, double longitude), `AvailabilityWindow.java` (record: LocalDateTime checkIn, LocalDateTime checkOut; validate `checkOut.isAfter(checkIn)` in compact constructor)
- [ ] T043 [P] [US3] Create enums in `catalog-service/src/main/java/org/labcabrera/parking/catalog/domain/model/`: `FacilityTag.java` (EXPRESS_ENTRY, FREE_CANCELLATION, COVERED, EV_CHARGING, GUARDED), `FacilityStatus.java` (ACTIVE, MAINTENANCE, CLOSED), `SpotType.java` (STANDARD, COMPACT, DISABLED, EV), `SpotAvailabilityStatus.java` (AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE)
- [ ] T044 [US3] Create `CancellationPolicy.java` (record: int freeCancelHours, int penaltyCancelMinutes) and `ParkingFacility.java` aggregate root in `catalog/domain/model/` with fields: `FacilityId id`, `String name`, `String city`, `String address`, `Coordinates location`, `int totalSpots`, `Set<FacilityTag> tags`, `FacilityStatus status`, `CancellationPolicy cancellationPolicy`, `@Version Long version`; add domain method `boolean isSearchable()` returning `status == ACTIVE`
- [ ] T045 [P] [US3] Create `ParkingSpot.java` entity in `catalog/domain/model/` with fields: `SpotId id`, `FacilityId facilityId`, `String spotNumber`, `SpotType type`, `SpotAvailabilityStatus availabilityStatus`, `@Version Long version`; add method `boolean isAvailable()` returning `availabilityStatus == AVAILABLE`
- [ ] T046 [P] [US3] Create port interfaces in catalog-service: `SearchParkingPort.java` (inbound, in `domain/port/inbound/`) with method `Page<FacilityResult> search(SearchParkingQuery query)`; `FacilityRepository.java` (outbound, in `domain/port/outbound/`) with methods `Page<ParkingFacility> findByTextAndStatus(String text, FacilityStatus status, Pageable pageable)` and `Optional<ParkingFacility> findById(FacilityId id)`; `AvailabilityCache.java` (outbound) with methods `Optional<Integer> getAvailableSpotCount(FacilityId id, AvailabilityWindow window)` and `void putAvailableSpotCount(FacilityId id, AvailabilityWindow window, int count, Duration ttl)`
- [ ] T047 [P] [US3] Create `AvailabilityDomainService.java` in `catalog/domain/service/` with method `boolean hasAvailability(ParkingFacility facility, int availableSpots)` — returns `availableSpots > 0 && facility.isSearchable()`; also add `boolean isLowAvailability(int available, int total)` returning `available <= total * 0.2`

### Catalog Application (US3)

- [ ] T048 [P] [US3] Create `SearchParkingQuery.java` record in `catalog/application/queries/` with fields: `String text`, `LocalDateTime checkIn`, `LocalDateTime checkOut`, `int page`, `int size`; add static factory `SearchParkingQuery.of(String text, LocalDateTime checkIn, LocalDateTime checkOut)` defaulting page=0, size=20
- [ ] T049 [US3] Create `SearchParkingQueryHandler.java` in `catalog/application/queries/` implementing `SearchParkingPort`; inject `FacilityRepository`, `AvailabilityCache`, `AvailabilityDomainService`; logic: check cache for each facility's availability count (cache-miss calls repository); map to `FacilityResult` DTO with `lowAvailabilityWarning` flag; return `Page<FacilityResult>`
- [ ] T050 [P] [US3] Create `FacilityResult.java` DTO record in `catalog/application/dto/` with fields: `String facilityId`, `String name`, `String city`, `String address`, `double latitude`, `double longitude`, `BigDecimal dailyRate`, `String currency`, `Set<String> tags`, `boolean lowAvailabilityWarning`, `int availableSpots`; create `SearchRequest.java` POJO with same fields as `SearchParkingQuery` for HTTP deserialization

### Catalog Infrastructure (US3)

- [ ] T051 [P] [US3] Create JPA entities in `catalog/infrastructure/persistence/`: `ParkingFacilityJpaEntity.java` annotated `@Entity @Table(name="parking_facility")` with all columns per V1 migration; `ParkingSpotJpaEntity.java` annotated `@Entity @Table(name="parking_spot")` with all columns; both with `@Version Long version`
- [ ] T052 [P] [US3] Create `FacilityJpaRepository.java` extending `JpaRepository<ParkingFacilityJpaEntity, UUID>` with `@Query` method `findByTextAndStatus` using full-text ILIKE search on name and city; create `FacilityJpaMapper.java` with static methods to convert between JPA entity and domain object
- [ ] T053 [US3] Create `FacilityRepositoryAdapter.java` in `catalog/infrastructure/persistence/` implementing `FacilityRepository` port; delegates to `FacilityJpaRepository`, converts entities via `FacilityJpaMapper`; annotated `@Repository` and `@Transactional(readOnly = true)`
- [ ] T054 [P] [US3] Create `RedisAvailabilityCache.java` in `catalog/infrastructure/cache/` implementing `AvailabilityCache` port; uses `RedisTemplate<String, String>`; cache key pattern: `availability:{facilityId}:{checkIn}:{checkOut}`; TTL 60s for availability counts, 30s for search result pages; serializes count as plain string
- [ ] T055 [P] [US3] Create `CatalogInfrastructureConfig.java` in `catalog/infrastructure/config/`: declare `RedisTemplate<String, String>` bean with `StringRedisSerializer`; declare `OpenAPI` bean (SpringDoc) with API title "Parking Catalog API", version "1.0.0", description; create `CatalogSecurityConfig.java` (`@Configuration @EnableWebSecurity`) permitting all requests to `/api/v1/catalog/**` and `/v3/api-docs/**` without authentication

### Catalog Interfaces (US3)

- [ ] T056 [US3] Create `CatalogController.java` in `catalog/interfaces/rest/` annotated `@RestController @RequestMapping("/api/v1/catalog")`; inject `SearchParkingPort`; implement `GET /search` with `@RequestParam` for `q` (required), `checkIn` and `checkOut` (ISO datetime, required), `page` (default 0), `size` (default 20); map params to `SearchParkingQuery`; return `ResponseEntity<Page<FacilityResult>>`; annotate with `@Operation(summary = "Search parking facilities")`, `@ApiResponse(responseCode = "200")`, `@Parameter` on each param
- [ ] T057 [P] [US3] Create `catalog-service/src/main/resources/db/migration/V2__seed_test_data.sql` inserting 3 sample parking facilities: `Madrid Centro` (city=Madrid, 50 spots, ACTIVE, EV_CHARGING+COVERED tags), `Barcelona Gràcia` (city=Barcelona, 30 spots, ACTIVE, FREE_CANCELLATION), `Madrid Barajas Airport` (city=Madrid, 100 spots, ACTIVE, EXPRESS_ENTRY+GUARDED); insert 5 AVAILABLE `parking_spot` rows per facility

### Frontend Search (US3)

- [ ] T058 [P] [US3] Create `frontend/src/types/catalog.ts` with TypeScript interfaces: `FacilityResult` (facilityId, name, city, address, latitude, longitude, dailyRate, currency, tags, lowAvailabilityWarning, availableSpots), `SearchRequest` (text, checkIn, checkOut, page?, size?), `FacilityTag` const enum
- [ ] T059 [P] [US3] Create `frontend/src/services/catalogApi.ts` exporting `searchParking(params: SearchRequest): Promise<FacilityResult[]>` using `fetch` against `${import.meta.env.VITE_CATALOG_SERVICE_URL}/api/v1/catalog/search` with URLSearchParams; throw on non-2xx response
- [ ] T060 [P] [US3] Create `frontend/src/hooks/useParkingSearch.ts` React Query hook using `useQuery`; key: `['parking-search', params]`; enabled only when `params.text && params.checkIn && params.checkOut`; wraps `catalogApi.searchParking`; staleTime 30 000ms
- [ ] T061 [US3] Create `frontend/src/pages/SearchPage.tsx` with: controlled text input for location, `<input type="datetime-local">` for check-in and check-out, submit button calling `useParkingSearch`; render loading spinner while fetching; render list of `FacilityResultCard` components showing name, city, dailyRate, tags as badges, availableSpots count; show "Low availability" warning badge when `lowAvailabilityWarning=true`; show error message on failure
- [ ] T062 [US3] Update `frontend/src/App.tsx` with React Router v6 `<Routes>`: route `/` renders `<SearchPage />`; add `QueryClientProvider` wrapping the app; add `frontend/.env.example` with `VITE_CATALOG_SERVICE_URL=http://localhost:8081` and `VITE_PAYMENT_GATEWAY_URL=http://localhost:3001`
- [ ] T063 [P] [US3] Configure `frontend/vite.config.ts` with `server.proxy`: `/api/v1/catalog` → `http://localhost:8081`; set `server.port=3000`; set `build.outDir=dist`

**Checkpoint**: `curl "http://localhost:8081/api/v1/catalog/search?q=madrid&checkIn=2026-07-01T10:00&checkOut=2026-07-03T18:00"` returns HTTP 200 with JSON array. `GET /v3/api-docs` returns HTTP 200. Frontend search form at `http://localhost:3000` fetches and renders facility cards.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Resilience, observability, and logging baseline across all three services.

- [ ] T064 [P] Create `ResilienceConfig.java` in `reservation-service/src/main/java/org/labcabrera/parking/reservation/infrastructure/config/`: declare `CircuitBreakerRegistry` bean with config for `pricingService` (slidingWindowSize=10, failureRateThreshold=50, waitDurationInOpenState=10s) and `paymentService` (same config); add corresponding `resilience4j.circuitbreaker` entries to `reservation-service/src/main/resources/application.yml`
- [ ] T065 [P] Create `logback-spring.xml` in each service under `src/main/resources/` (3 files): for profile `prod` use Logstash JSON encoder outputting structured log with fields `service`, `level`, `message`, `traceId`, `spanId`; for profile `default` use standard console pattern
- [ ] T066 [P] Add OpenTelemetry Java agent entry to each service `Dockerfile`: `ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar` then `ENV JAVA_TOOL_OPTIONS=-javaagent:/otel/opentelemetry-javaagent.jar`; add `OTEL_EXPORTER_OTLP_ENDPOINT` env var to each service in `docker-compose.yml`; add `otel-collector` service stub using `otel/opentelemetry-collector:latest`
- [ ] T067 [P] Add `prometheus` (prom/prometheus:latest) and `grafana` (grafana/grafana:latest) service stubs to `docker-compose.yml`; add Prometheus scrape config `infrastructure/prometheus/prometheus.yml` targeting each Spring Boot service `/actuator/prometheus`; add `management.prometheus.metrics.export.enabled=true` to each service `application.yml`
- [ ] T068 Validate `quickstart.md` end-to-end: run `./gradlew build`, run `docker compose up`, verify each service health endpoint returns 200, verify `GET /v3/api-docs` returns 200 for each service, verify `http://localhost:3000` loads, verify `http://localhost:3001` loads, verify search curl returns data — document any discrepancies found in quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 completion — **BLOCKS all user stories**
- **Phase 3 (US1)**: Depends on Phase 2 — Dockerfiles require compilable service skeletons
- **Phase 4 (US2)**: Depends on Phase 2 — ArchUnit requires 4-layer package structure to exist
- **Phase 5 (US3)**: Depends on Phase 2 — domain model builds on package skeleton
- **Phase 6 (Polish)**: Depends on Phase 3 (services running) and Phase 5 (catalog service complete)

### User Story Dependencies

- **US1 (P1)**: Phase 2 complete → US1 can begin. No dependency on US2 or US3
- **US2 (P2)**: Phase 2 complete → US2 can begin. No dependency on US1 or US3
- **US3 (P3)**: Phase 2 complete → US3 can begin. No dependency on US1 or US2

### Within Phase 5 (US3) Execution Order

- Domain layer (T042–T047) must complete before Application layer (T048–T050)
- Application layer must complete before Infrastructure (T051–T055) and Interfaces (T056)
- Seed data (T057) must be added before end-to-end test
- Frontend tasks (T058–T063) can run in parallel to backend US3 tasks

---

## Parallel Execution Examples

**Phase 2 (Foundational) — 3 threads**:
```
Thread A: T010 → T011 → T012 → T019
Thread B: T013 → T014 → T015 → T020
Thread C: T016 → T017 → T018 → T021
```

**Phase 5 (US3) — backend vs frontend**:
```
Thread A (backend): T042 → T044 → T046 → T047 → T048 → T049 → T050 → T051 → T053 → T054 → T055 → T056 → T057
Thread B (frontend): T058 → T059 → T060 → T061 → T062 → T063
Thread C (domain parallel): T043 → T045
```

**Phase 4 (US2) — fully parallel after T034**:
```
Thread A: T034
Thread B: T035 (after Phase 2)
Thread C: T036 (after Phase 2)
Thread D: T037 → T038 → T039 → T040 → T041 (all parallel)
```

---

## Implementation Strategy

**MVP scope**: Phase 1 + Phase 2 + Phase 3 (US1) — complete runnable system skeleton with all containers healthy.

**Incremental delivery**:
1. **Iteration 1** — Phase 1 + Phase 2: Compilable Gradle monorepo with 4-layer package structure
2. **Iteration 2** — Phase 3 (US1): Full `docker compose up` environment, all health checks passing
3. **Iteration 3** — Phase 4 (US2): Architecture documentation and ArchUnit gates passing (SC-007)
4. **Iteration 4** — Phase 5 (US3): First working business feature — parking search end-to-end (SC-008, SC-009)
5. **Iteration 5** — Phase 6 (Polish): Production-grade observability, resilience configuration, quickstart validation
