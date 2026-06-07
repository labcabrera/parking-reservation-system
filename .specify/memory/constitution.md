<!--
  SYNC IMPACT REPORT
  ==================
  Version change:         1.0.1 → 2.0.0
  Modified principles:
    - Technology Stack: Maven → Gradle (backend build tool)
    - Technology Stack: Flyway removed from Persistence section
  Removed sections:
    - III. Test-First Discipline (NON-NEGOTIABLE) — removed by amendment
  Renumbered:
    - IV. Concurrency & Distributed Data Consistency → III
    - V.  Performance & Resilience                 → IV
    - VI. Observability                            → V
    - VII. Language Discipline                     → VI
  Templates requiring updates:
    - .specify/templates/plan-template.md  ✅ no changes required
    - .specify/templates/spec-template.md  ✅ no changes required
    - .specify/templates/tasks-template.md ✅ no changes required
  Deferred items:         none
-->

# Parking Reservation System Constitution

## Core Principles

### I. Hexagonal Architecture & Clean Architecture (NON-NEGOTIABLE)

All business logic MUST reside in the **domain** and **application** layers, completely
isolated from any infrastructure concern. The dependency rule MUST always point inward:
infrastructure → application → domain.

- The **domain** layer contains entities, value objects, aggregates, domain services, and
  repository interfaces (ports). It MUST have zero dependencies on frameworks, databases,
  HTTP, or any external library beyond standard language primitives.
- The **application** layer contains use cases (interactors) and application services. It
  orchestrates domain objects and invokes ports. It MUST NOT import Spring annotations,
  JPA, or any infrastructure class directly.
- The **infrastructure** layer contains adapters (REST controllers, JPA repositories,
  message producers/consumers, payment gateway clients). Adapters MUST only depend on
  ports defined in the application/domain layers.
- Cross-layer imports in the wrong direction are a **build-breaking violation** and MUST
  be caught by ArchUnit tests running in CI.
- Package structure MUST reflect layer separation (root package `org.labcabrera.parking.<service>`):
  - `org.labcabrera.parking.<service>.domain` — entities, value objects, aggregates, ports, domain services
  - `org.labcabrera.parking.<service>.application` — command handlers, query handlers, application DTOs
  - `org.labcabrera.parking.<service>.infrastructure` — adapters (web, persistence, messaging, external)
  - `org.labcabrera.parking.<service>.interfaces` — inbound adapters (REST controllers, Kafka consumers)

### II. Domain-Driven Design

The domain model is the single source of truth for all business rules and invariants.

- `ParkingSpot`, `Reservation`, `Payment`, `Facility`, and `Availability` are first-class
  **aggregates** or **entities**. Their boundaries MUST enforce invariants without relying
  on database constraints or application-layer checks alone.
- Aggregates MUST encapsulate the overselling-prevention invariant: a spot MUST NOT be
  double-booked under concurrent requests. This MUST be enforced within the aggregate
  root before any state is persisted.
- Value objects (e.g., `SpotId`, `ReservationPeriod`, `Money`) MUST be immutable.
- Domain events (e.g., `ReservationConfirmed`, `PaymentProcessed`, `SpotReleased`) MUST
  be raised by aggregate roots for cross-aggregate communication and eventually-consistent
  workflows.
- Ubiquitous language terms defined in this constitution MUST be used consistently in
  code, tests, API contracts, documentation, and team communication.

### III. Concurrency & Distributed Data Consistency

The system operates at a scale where concurrent access to the same resource is inevitable.
Data integrity MUST be guaranteed across all failure modes.

- Optimistic locking (`@Version` or equivalent) MUST be applied to all aggregates subject
  to concurrent modification (`Reservation`, `ParkingSpot` availability state).
- Multi-step operations spanning domain aggregates or external services MUST be implemented
  using the **SAGA pattern** (choreography or orchestration) with explicit compensating
  transactions defined for each step.
- The **Transactional Outbox pattern** MUST be used to guarantee at-least-once delivery of
  domain events to message brokers, preventing data loss on infrastructure failure.
- Idempotency keys MUST be enforced on all reservation-creation and payment endpoints to
  prevent duplicate processing on retries.
- Distributed caches (e.g., Redis) used for availability queries MUST implement a
  cache-aside pattern with TTL and explicit invalidation strategies documented per use case.

### IV. Performance & Resilience

The system MUST sustain millions of parking spots distributed across cities and airports,
and handle massive concurrent traffic spikes (e.g., holiday season openings, mass events).

- Availability search endpoints MUST achieve p95 latency ≤ 100 ms under peak production
  load. Load tests validating this threshold MUST be part of the definition-of-done for
  all search-critical features.
- All external integrations (payment gateways, third-party data providers) MUST be wrapped
  in a **Circuit Breaker** with defined open/half-open thresholds and explicit fallback
  strategies.
- Retry logic with exponential back-off and jitter MUST be applied to all retriable
  external calls. Maximum retry attempts and back-off caps MUST be configurable per
  integration.
- Horizontal scalability MUST be the default design assumption: no in-process shared
  mutable state, no sticky sessions. All session and availability state MUST be
  externalized (Redis, database).
- Database queries on hot paths MUST have reviewed index coverage before merging.

### V. Observability

The system MUST be fully observable in production. Silent failures are forbidden.

- Structured JSON logging MUST be used throughout. Log entries MUST include `traceId`,
  `spanId`, `userId` (when available), and `reservationId` (when applicable).
- Distributed tracing via OpenTelemetry MUST be instrumented on all use-case entry points
  and adapter calls (DB, messaging, external HTTP).
- Application metrics (reservation rate, payment success/failure ratio, circuit breaker
  state, cache hit ratio, SAGA compensation rate) MUST be exposed via
  `/actuator/prometheus` or an equivalent metrics endpoint.
- Alerts MUST be defined for: payment failure rate > 1%, p95 search latency > 150 ms,
  and any circuit breaker transitioning to open state.
- Swallowed exceptions without logging or metrics increment are a **code-review blocking
  violation**.

### VI. Language Discipline

All project artifacts MUST be produced in English without exception.

- Source code (identifiers, comments, Javadoc, JSDoc) MUST be in English.
- Documentation (README, architecture decision records, API contracts, this constitution,
  commit messages) MUST be in English.
- API request/response field names and error messages exposed to clients MUST be in
  English.
- Test names, assertion messages, and test data labels MUST be in English.

## Technology Stack & Constraints

**Backend**: Spring Boot 4.x (Java 21+). Spring MVC for REST controllers. Spring Security
for authentication and authorization (OAuth2 / JWT). Spring Data JPA for persistence
adapters. Spring Kafka or Spring AMQP for messaging adapters.

**Frontend**: React (latest stable LTS). TypeScript MUST be used for all frontend code.
Server state MUST be managed with React Query. Routing via React Router v6+.

**Persistence**: PostgreSQL as the primary relational store. Redis for distributed caching
and distributed lock support.

**Messaging**: Apache Kafka for domain event streaming and SAGA coordination between
bounded contexts.

**Build & CI**: Gradle (backend). npm / Vite (frontend). GitHub Actions for CI/CD. All
builds MUST pass linting, unit tests, and integration tests before a pull request can be
merged.

**Containerization**: Docker. All services MUST be Kubernetes-compatible and adhere to
12-factor app principles (externalized configuration, stateless processes, ephemeral
storage).

**Security**: OWASP Top 10 MUST be addressed at design time. JWT tokens MUST be
short-lived (≤ 1 hour). Secrets MUST be managed via environment variables or a dedicated
secrets manager; hardcoded credentials are a **build-breaking violation**.

## Development Workflow

1. A **feature branch** MUST be created from `develop` following the naming convention
   `###-short-description` (e.g., `001-parking-spot-search`).
2. A **Constitution Check** MUST be performed and passed before any planning begins.
3. The workflow MUST follow the sequence: **Specify** → **Clarify** → **Plan** →
   **Tasks** → **Implement** using the corresponding Spec Kit commands.
4. **Code review** MUST have a minimum of 1 approval. The reviewer MUST verify
   constitution compliance and the absence of cross-layer dependency violations
   (enforced by ArchUnit).
5. **Quality gates** — spec quality, plan coverage, task traceability, and completeness
   review — MUST all pass before a feature branch is merged into `develop`.
6. Merges to `develop` trigger the full CI pipeline (build, unit tests, integration tests,
   Testcontainers-based infrastructure tests).

## Governance

This constitution supersedes all other development guidelines for this project. Any
practice conflicting with a principle herein MUST be discontinued immediately upon
ratification.

**Amendment procedure**: Amendments MUST be proposed as a pull request modifying this
file, accompanied by a written rationale. The version MUST be incremented following the
semantic versioning rules below. Amendments that affect Principles I or II
(architecture or DDD) require explicit team consensus before merging.

**Versioning policy**:

- MAJOR — backward-incompatible principle removals or redefinitions.
- MINOR — new principle or section added, or materially expanded guidance.
- PATCH — clarifications, wording improvements, or typo fixes.

**Compliance review**: Constitution compliance MUST be verified at every pull request
review. Repeated violations MUST be escalated and resolved via an amendment or a
documented team decision.

**Version**: 2.0.0 | **Ratified**: 2026-06-01 | **Last Amended**: 2026-06-07
