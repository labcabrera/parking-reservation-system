# Feature Specification: System Modules Definition & Initial Structure

**Feature Branch**: `001-system-modules-initial-structure`
**Created**: 2026-06-01
**Status**: Draft
**Input**: `refined-instructions.md` — Parking Reservation System high-level requirements

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Developer bootstraps and runs the full system locally (Priority: P1)

A developer clones the monorepo and, with a single command (`docker compose up`), has the
full system running locally: Keycloak, all backend services, the React frontend, databases,
message broker, and Redis. Within minutes the developer can hit the health check endpoint
of each service and open the frontend in a browser.

**Why this priority**: Every other user story and every subsequent feature depends on a
functional, runnable local environment. Without this, no other work can be integrated or
validated.

**Independent Test**: Run `docker compose up` from the repository root. All containers
reach a healthy state. The frontend loads at its local port. Each backend service responds
to its health check endpoint with a 200 status.

**Acceptance Scenarios**:

1. **Given** the monorepo is cloned on a machine with Docker and Docker Compose installed,
   **When** the developer runs `docker compose up`,
   **Then** all services start without errors, health checks pass, and the frontend is
   reachable in the browser.

2. **Given** the system is running,
   **When** the developer navigates to each service's health endpoint,
   **Then** each service returns a healthy status indicating it is connected to its
   required dependencies (database, cache, broker).

3. **Given** the system is running,
   **When** the developer accesses the Keycloak admin console,
   **Then** the pre-configured realm, roles (Customer, Administrator), and a seed test
   user are present.

---

### User Story 2 — Architect reviews module boundaries and integration contracts (Priority: P2)

A technical architect can inspect the monorepo structure and understand, without reading
implementation code, which bounded contexts exist, what each module is responsible for,
and how they communicate with each other.

**Why this priority**: Correct module decomposition is the architectural foundation. All
future feature work must fit within the boundaries defined here.

**Independent Test**: The repository contains a top-level directory per module. Each
module contains an `architecture decision record` (ADR) or README that states its
responsibility, its inbound/outbound ports, and its integration dependencies.

**Acceptance Scenarios**:

1. **Given** the repository is open,
   **When** the architect lists the top-level directories,
   **Then** each major bounded context (Catalog, Reservation, Payment, Frontend,
   Infrastructure) is represented by a dedicated directory.

2. **Given** the repository is open,
   **When** the architect reads the documentation for any backend module,
   **Then** the module's hexagonal layers (domain, application, infrastructure) are
   identifiable from the package/directory structure, with no cross-layer import
   violations.

3. **Given** the architecture documentation exists,
   **When** the architect reviews the integration diagram,
   **Then** synchronous (REST) and asynchronous (events/SAGA) integration points between
   modules are clearly identified and labelled.

---

### User Story 3 — Customer searches for available parking without logging in (Priority: P3)

An unauthenticated visitor uses the search form on the frontend to find available parking
near a location with entry and exit dates. The frontend queries the Catalog Service and
displays results.

**Why this priority**: Search is the entry point of the entire user journey. Although this
is a detailed functional story, validating the end-to-end path from frontend → Catalog
Service → database confirms the skeleton is wired correctly.

**Independent Test**: Open the frontend, fill in the search form with a text query and
dates, submit it. The Catalog Service returns a list of results and the frontend renders
them, even if the data is seeded mock data.

**Acceptance Scenarios**:

1. **Given** the system is running with seeded parking data,
   **When** an unauthenticated user submits a free-text search with entry and exit dates,
   **Then** the frontend displays a list of matching parking options with name, rate,
   tags, low-availability warning (if applicable), and geolocation data.

2. **Given** no matching parkings exist for the query,
   **When** the user submits the search,
   **Then** the frontend displays an empty-state message rather than an error.

3. **Given** the system is under load,
   **When** multiple simultaneous search requests arrive,
   **Then** all receive a response within the target latency threshold without errors.

---

### Edge Cases

- What happens when a module's dependency (e.g., the broker or cache) is unavailable at startup?
  The service MUST start in a degraded state and expose this via its health check rather
  than refusing to start, allowing the rest of the system to be operational.
- What happens if Docker Compose is run without pre-built images?
  The Compose file MUST include build directives so images are built automatically on
  first run.
- What happens if the seed data script fails?
  The system MUST still start; seed failures MUST be logged clearly and MUST NOT crash
  the service.

---

## Clarifications

### Session 2026-06-01

- Q: What strategy MUST the system use to prevent double-booking under concurrent requests? → A: Axon aggregate serialization + DB optimistic locking — Axon enforces single-threaded command handling per aggregate instance; a `@Version` column on `ParkingSpot` provides persistence-level protection without requiring an external distributed lock on the hot path.
- Q: What rules determine whether a reservation is eligible for cancellation (with or without penalty)? → A: Time-based with configurable penalty window — free cancellation up to N hours before check-in (configurable); penalised cancellation (partial refund) from N hours down to M minutes before check-in (configurable); no cancellation inside M minutes of check-in.
- Q: Who owns Keycloak account creation during the self-registration flow — the frontend directly or a backend API? → A: The frontend calls a dedicated backend Registration API; the backend holds Keycloak admin credentials and creates the account via the Keycloak Admin REST API server-side. No Keycloak admin secret is ever exposed to the browser.
- Q: What happens when the price-lock window expires before the user completes payment? → A: Auto-cancel on expiry — the SAGA deadline fires; the reservation transitions `PENDING → EXPIRED`; the spot lock is released; the user must initiate a new reservation.
- Q: Should the search p95 latency target in SC-002 be 200 ms or align with the constitution's 100 ms threshold? → A: Align to constitution — SC-002 updated to p95 ≤ 100 ms; the Catalog Service MUST use a Redis cache-aside pattern on the availability index hot path to meet this target.
- Q: What build tool MUST be used for backend Java modules? → A: Gradle with integrated `gradlew` wrapper using Groovy DSL (`*.gradle`) — the monorepo MUST use a Gradle multi-project build with Groovy DSL; Kotlin DSL (`*.gradle.kts`) MUST NOT be used; Maven is not used.
- Q: What are the exact Java and Spring Boot versions? → A: Java 21 and Spring Boot 4.0.6 — all backend services MUST target Java 21 and declare `org.springframework.boot` version `4.0.6`.
- Q: How is the mock payment gateway implemented and integrated with the main frontend? → A: Separate React SPA (`payment-gateway/`) — the main frontend redirects to the gateway with reservation and payment context via URL parameters; the gateway displays a mock payment form, calls the Payment Service API to record the outcome, and redirects back to the main frontend with a result parameter (`?result=approved` or `?result=declined`). Both SPAs run as independent Vite projects.
- Q: How MUST backend REST APIs be documented? → A: Code-first with SpringDoc — each Spring Boot service MUST include `springdoc-openapi` and expose the generated OpenAPI 3.x spec at `/v3/api-docs` and the Swagger UI at `/swagger-ui.html`.
- Q: What are the four hexagonal package layers and how is the application layer structured? → A: Four layers — `domain` (aggregates, VOs, domain services, port interfaces), `application` (command handlers + query handlers using commands/queries CQRS model; no use-case classes), `infrastructure` (outbound adapters: JPA, Redis, external HTTP clients, Kafka producers, Keycloak adapter, Spring config), `interfaces` (inbound adapters: REST controllers, Kafka consumers). Use-case pattern MUST NOT be used.- Q: What is the root Java package for all backend services? → A: `org.labcabrera.parking` — all backend Java modules MUST use `org.labcabrera.parking.<service>` as their root package (e.g. `org.labcabrera.parking.catalog`, `org.labcabrera.parking.reservation`, `org.labcabrera.parking.payment`).
---

## Requirements *(mandatory)*

### Functional Requirements

**Monorepo Structure**

- **FR-001**: The repository MUST be organized as a monorepo containing all modules:
  `catalog-service`, `reservation-service`, `payment-service`, `frontend`, and shared
  infrastructure configuration.
- **FR-002**: Each backend module MUST implement hexagonal architecture with four clearly
  separated packages:
  - `domain` — aggregate roots, value objects, domain services, and port interfaces
    (`port/inbound/`, `port/outbound/`)
  - `application` — command handlers and query handlers following the commands/queries
    CQRS model (packages `commands/` and `queries/`); use-case classes MUST NOT be used
  - `infrastructure` — outbound adapters: JPA persistence, Redis cache, external HTTP
    clients, Kafka producers / Outbox publisher, Keycloak adapter, Spring configuration
  - `interfaces` — inbound adapters: REST controllers, Kafka consumers
  ArchUnit MUST enforce that no class in `domain` depends on `application`,
  `infrastructure`, or `interfaces`; no class in `application` depends on
  `infrastructure` or `interfaces`.
- **FR-003**: The monorepo MUST include a single `docker-compose.yml` at the repository
  root that starts the entire system (all services, databases, cache, broker, Keycloak).
- **FR-034**: All backend Java modules MUST use Gradle as the build tool with **Groovy
  DSL** (`settings.gradle`, `build.gradle`). Kotlin DSL (`*.gradle.kts`) MUST NOT be
  used. The monorepo MUST use a Gradle multi-project build with a root `settings.gradle`
  and a root `build.gradle` for shared dependency management. An integrated `gradlew`
  wrapper MUST be committed to the repository so the build can run without a local Gradle
  installation. Maven MUST NOT be used. All backend services MUST target **Java 21** and
  declare **Spring Boot 4.0.6** as the parent/platform version.

**Catalog Service**

- **FR-004**: The Catalog Service MUST expose a read API for searching parking facilities
  by free text, entry date/time, and exit date/time.
- **FR-005**: Search results MUST include: facility name, applicable rate, feature tags,
  low-availability warning, and coordinates for map rendering.
- **FR-006**: The Catalog Service MUST be queryable without user authentication.
- **FR-007**: The Catalog Service MUST maintain a queryable availability index that
  supports high-throughput read operations with low latency.

**Reservation Service**

- **FR-008**: The Reservation Service MUST manage the full reservation lifecycle:
  creation, confirmation, and cancellation. Cancellation eligibility MUST follow a
  time-based policy with two configurable thresholds (`free-cancel-hours` and
  `penalty-cancel-minutes`): free cancellation is allowed up to `free-cancel-hours`
  before check-in; penalised cancellation (partial refund calculated by the Pricing
  Service) is allowed from `free-cancel-hours` down to `penalty-cancel-minutes` before
  check-in; cancellation is disallowed inside the `penalty-cancel-minutes` window.
- **FR-009**: The Reservation Service MUST request a price quote from an external Pricing
  Service at reservation initiation; the quoted price MUST be locked for a configurable
  time window (`price-lock-minutes`). When the window expires before payment is completed,
  the SAGA MUST fire a deadline, transition the reservation to `EXPIRED`, release the
  spot lock, and notify the user that they must start a new reservation.
- **FR-010**: The Reservation Service MUST guarantee that a single parking spot cannot be
  double-booked under concurrent requests (oversell prevention). The mechanism MUST be
  Axon aggregate command serialization (per aggregate instance) combined with a `@Version`
  optimistic lock on the `ParkingSpot` persistence entity. On optimistic lock conflict the
  command handler MUST throw a domain exception that the SAGA compensates.
- **FR-011**: Reservation state transitions MUST be: `PENDING` → `CONFIRMED` |
  `REJECTED` | `CANCELLED` | `EXPIRED`. The `EXPIRED` state is reached when the
  price-lock deadline fires before payment is completed (FR-009). `EXPIRED` is a
  terminal state; the associated spot lock MUST be released upon entry.
- **FR-012**: The Reservation Service MUST publish domain events for each state
  transition so that downstream services (Payment, Notification) can react asynchronously.
- **FR-013**: The Reservation Service MUST implement the SAGA pattern to coordinate the
  multi-step reservation + payment flow, including compensating transactions on failure.
  The SAGA MUST register an Axon deadline for `price-lock-minutes` at the moment the
  price quote is locked; if the deadline fires before a `PAYMENT_APPROVED` event arrives,
  the SAGA MUST execute the compensating transaction (release spot, set state `EXPIRED`).

**Payment Service (Simulator)**

- **FR-014**: The Payment Service MUST expose available payment methods (mocked).
- **FR-015**: The Payment Service MUST process payment requests asynchronously and emit
  a result event (`PAYMENT_APPROVED` | `PAYMENT_DECLINED`).
- **FR-016**: The Reservation Service MUST react to Payment Service events and transition
  reservation state accordingly (`PENDING` → `CONFIRMED` on approval,
  `PENDING` → `REJECTED` on decline).
- **FR-017**: The Payment Service MUST implement circuit breaker behavior so that
  downstream failures do not cascade to the Reservation Service.

**User & Authentication**

- **FR-018**: Authentication and authorization MUST be delegated to Keycloak using
  OAuth2 / OIDC.
- **FR-019**: The system MUST define two roles in Keycloak: `CUSTOMER` and
  `ADMINISTRATOR`.
- **FR-020**: Unauthenticated users MUST be able to search for parking and view
  availability.
- **FR-021**: Only authenticated users MUST be able to create or cancel reservations.
- **FR-022**: User creation during the reservation flow MUST be handled by a backend
  Registration API endpoint. The backend MUST create the Keycloak account by calling the
  Keycloak Admin REST API using server-side credentials. The Keycloak admin client secret
  MUST NOT be exposed to the frontend at any point. On successful registration the backend
  MUST return a short-lived JWT so the user is immediately authenticated.
- **FR-023**: All personal data (name, email, vehicle plate) and payment data MUST be
  stored and transmitted following data minimization and encryption-at-rest practices.

**Frontend**

- **FR-024**: The frontend MUST provide a parking search form (free text, entry/exit
  dates) accessible to unauthenticated users.
- **FR-025**: The frontend MUST show an inline registration form (name, email, vehicle
  plate, invoice checkbox, marketing consent checkbox, terms & conditions acceptance)
  when an unauthenticated user attempts to start a reservation. Submission MUST call the
  backend Registration API (FR-022); the frontend MUST NOT interact with Keycloak
  directly.
- **FR-026**: The frontend MUST display the reservation status page after payment
  completion.
- **FR-027**: Authenticated customers MUST be able to view their reservation history and
  cancel eligible reservations. The UI MUST clearly indicate whether a cancellation is
  free, penalised, or not allowed, based on the time-based policy defined in FR-008.
- **FR-028**: Authenticated administrators MUST be able to view the parking CRUD,
  facility status, and cancellation reports (with no sensitive user data).
- **FR-029**: The frontend MUST display real-time availability updates (e.g., low-spot
  warnings) without requiring a full page reload.
- **FR-036**: The system MUST include a second React SPA (`payment-gateway`) that acts as
  the mock payment gateway. When a user selects a payment method in the main frontend,
  the main frontend MUST redirect the browser to the `payment-gateway` application,
  passing the following as URL query parameters: `reservationId`, `paymentId`, `amount`,
  `currency`, and `returnUrl` (the full URL of the main frontend reservation page).
- **FR-037**: The `payment-gateway` SPA MUST display a mock payment form showing the
  amount and selected payment method. On user confirmation, it MUST call the Payment
  Service API to register the payment outcome and then redirect the browser to the
  `returnUrl` appending `?result=approved` or `?result=declined`. The main frontend MUST
  read this `result` parameter on load and display the final reservation status
  accordingly. The `payment-gateway` is a standalone Vite project; it does NOT share
  source code with the main frontend.

**Observability & Resilience**

- **FR-030**: Every backend service MUST expose a health check endpoint.
- **FR-031**: Every backend service MUST expose structured metrics consumable by a
  monitoring tool.
- **FR-032**: The system MUST use distributed tracing with a correlation ID propagated
  across all service calls.
- **FR-033**: The system MUST implement retry with exponential back-off and circuit
  breaker patterns on all inter-service HTTP and external integrations.
- **FR-035**: Every backend Spring Boot service MUST adopt a code-first OpenAPI strategy
  using `springdoc-openapi`. Each service MUST expose its generated OpenAPI 3.x
  specification at `/v3/api-docs` and the interactive Swagger UI at `/swagger-ui.html`.
  API annotations (`@Operation`, `@ApiResponse`, `@Schema`) MUST be kept on DTOs and
  controller methods; no hand-written OpenAPI YAML files are produced.

---

### Key Entities

- **ParkingFacility**: Represents a physical parking location (airport, city car park,
  etc.) with coordinates, capacity, tags, and operational status.
- **ParkingSpot**: An individual bookable space within a `ParkingFacility`, with
  availability state and type metadata.
- **Reservation**: A booking of one `ParkingSpot` for a defined period. Owns the
  lifecycle state machine and the price-lock record.
- **PriceLock**: A time-bounded agreed price attached to an in-progress reservation,
  obtained from the Pricing Service.
- **Payment**: A record of a payment attempt associated with a `Reservation`, including
  method, status, and external reference.
- **User**: A registered user with personal data (name, email, vehicle plate, billing
  preference, marketing consent). Identity is owned by Keycloak; the system stores only
  the non-identity attributes.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A developer can run `docker compose up` and have all services healthy within
  5 minutes on a standard development machine.
- **SC-002**: The free-text parking search returns results within 100 ms at p95 under a
  load representative of expected peak usage. The Catalog Service MUST employ a
  Redis cache-aside pattern on the availability index hot path to meet this target.
- **SC-003**: Zero double-bookings occur during a concurrency stress test simulating 500
  simultaneous reservation attempts for the same parking spot.
- **SC-004**: When the Payment Service is unavailable, in-flight reservations remain in
  `PENDING` state and the SAGA compensating transaction correctly releases the spot lock
  after the configured timeout.
- **SC-005**: All backend services recover automatically (circuit breaker transitions to
  closed) within 30 seconds of a downstream dependency becoming healthy again.
- **SC-006**: An administrator can view a cancellation report from the admin panel that
  contains no user PII.
- **SC-007**: All module boundaries are validated by automated architecture tests; zero
  cross-layer import violations exist in the codebase.
- **SC-008**: Each backend service MUST successfully serve its OpenAPI 3.x document at
  `/v3/api-docs` (HTTP 200) in all environments where the service is running, including
  the local Docker Compose stack.
- **SC-009**: The end-to-end redirect flow (main frontend → payment-gateway → main
  frontend) MUST complete without browser errors; the `result` parameter MUST be present
  on the return URL and the main frontend MUST render the correct reservation status for
  both `approved` and `declined` outcomes.

---

## Assumptions

- The Pricing Service is an external system; for the scope of this specification it is
  represented by a stub/mock that returns configurable prices with a time-window field.
- The real-time map display on the frontend uses a third-party map provider (e.g.,
  Leaflet + OpenStreetMap); the integration details are out of scope for this specification.
- Email and SMS notifications for reservation confirmation are out of scope for the initial
  structure but event contracts MUST be defined so they can be added without modifying
  existing services.
- Horizontal scaling configuration (Kubernetes manifests, replica counts) is out of scope
  for this specification; the `docker-compose.yml` targets a single-node development
  environment.
- Payments are fully mocked; no real payment processor integration is required.
- The Keycloak realm, roles, and a seed test user MUST be provisioned automatically via
  realm import on container startup.
- The `payment-gateway` SPA runs on its own port in the local Docker Compose stack and
  is accessed by the main frontend via a fully-qualified URL configured as an environment
  variable (`VITE_PAYMENT_GATEWAY_URL`). No authentication is required to access the
  payment gateway.
