# Implementation Plan: System Modules Definition & Initial Structure

**Branch**: `001-system-modules-initial-structure` | **Date**: 2026-06-01 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-system-modules-initial-structure/spec.md`

## Summary

Scaffold the complete monorepo for the Parking Reservation System: three Spring Boot 4.x
backend services (`catalog-service`, `reservation-service`, `payment-service`) following
hexagonal architecture, a React + TypeScript frontend, shared infrastructure configuration
(Keycloak, Kafka, PostgreSQL, Redis), and a single `docker-compose.yml` that boots the
entire ecosystem with one command. The architecture is built on Axon Framework aggregates
(no event sourcing) for command handling and SAGA orchestration, Redis cache-aside for
sub-100 ms availability queries, and Keycloak for OAuth2/OIDC authentication.

## Technical Context

**Language/Version**: Java 21 (backend) · TypeScript 5.x / Node 20 LTS (frontend)
**Primary Dependencies**: Spring Boot 4.x · Axon Framework 4.10+ · Spring Security OAuth2 Resource Server · Spring Data JPA · Spring Kafka · Resilience4j · Flyway · React 18+ · React Query v5 · React Router v6 · Vite 5 · React Testing Library · Vitest · ArchUnit 1.x · Testcontainers 1.x · Keycloak 24+
**Storage**: PostgreSQL 16 (primary relational store) · Redis 7 (cache-aside + optional distributed lock)
**Testing**: JUnit 5 + Mockito (backend unit) · Testcontainers + Spring Boot Test (integration) · ArchUnit (layer enforcement) · Vitest + React Testing Library (frontend)
**Target Platform**: Linux server — Docker / Kubernetes-compatible, 12-factor principles
**Project Type**: Monorepo — multi-module web application (Spring MVC REST × 3 services + React SPA)
**Performance Goals**: Search p95 ≤ 100 ms (cache-hit path) · Reservation creation p95 ≤ 500 ms
**Constraints**: No sticky sessions · No in-process shared state · JWT lifetime ≤ 1 hour · Keycloak admin secret server-side only · Secrets via environment variables · OWASP Top 10 addressed at design time
**Scale/Scope**: Millions of parking spots across cities and airports · Traffic spikes (holiday openings, mass events) · 3 backend services + 1 React SPA + 5 infrastructure components

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Gate | Status |
|-----------|------|--------|
| I. Hexagonal Architecture | domain/application/infrastructure per service; no outward dependency violations; ArchUnit in CI | ✅ PASS |
| II. DDD | Axon aggregates own invariants; domain events raised by aggregate roots; immutable VOs | ✅ PASS |
| III. TDD | Tests written first; ≥ 90% domain coverage gate in CI; Testcontainers for infra tests | ✅ PASS |
| IV. Concurrency & Consistency | Axon command serialization + `@Version` optimistic lock + Transactional Outbox + idempotency keys | ✅ PASS |
| V. Performance & Resilience | Redis cache-aside on Catalog hot path (p95 ≤ 100 ms); Resilience4j CB on all external integrations | ✅ PASS |
| VI. Observability | Actuator + Micrometer + Prometheus + OpenTelemetry Java agent; structured JSON logs | ✅ PASS |
| VII. Language | All code, docs, API contracts, test labels in English | ✅ PASS |

**Result: ALL GATES PASS — Phase 0 research proceeds.**

## Project Structure

### Documentation (this feature)

```text
specs/001-system-modules-initial-structure/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── catalog-api.md
│   ├── reservation-api.md
│   ├── payment-api.md
│   ├── registration-api.md
│   └── domain-events.md
└── tasks.md             # Phase 2 output (not created by /speckit.plan)
```

### Source Code (repository root)

```text
/ (repository root)
├── docker-compose.yml                          # Single entry point for full local stack
├── pom.xml                                     # Parent Maven POM — dependency management
│
├── catalog-service/                            # Bounded Context: Catalog & Availability
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/parking/catalog/
│       │   ├── domain/
│       │   │   ├── model/                      # ParkingFacility (aggregate root), ParkingSpot, value objects
│       │   │   ├── port/
│       │   │   │   ├── inbound/                # SearchParkingPort
│       │   │   │   └── outbound/               # FacilityRepository, AvailabilityCache
│       │   │   └── service/                    # AvailabilityDomainService
│       │   ├── application/
│       │   │   ├── usecase/                    # SearchParkingUseCase
│       │   │   └── dto/                        # SearchRequest, FacilityResult
│       │   └── infrastructure/
│       │       ├── web/                        # CatalogController (REST adapter)
│       │       ├── persistence/                # JPA entities, FacilityJpaRepository, mappers
│       │       ├── cache/                      # RedisAvailabilityCache (cache-aside adapter)
│       │       └── config/                     # Spring beans, Security config (permit-all for search)
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/                   # Flyway V1__create_catalog_schema.sql
│       └── test/java/com/parking/catalog/
│           ├── domain/                         # Pure unit tests (no Spring context)
│           ├── application/                    # Use-case unit tests (mocked ports)
│           └── infrastructure/                 # Testcontainers (PostgreSQL, Redis)
│
├── reservation-service/                        # Bounded Context: Reservations & SAGA
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/parking/reservation/
│       │   ├── domain/
│       │   │   ├── model/                      # Reservation (Axon @Aggregate), PriceLock VO, ReservationPeriod VO, Money VO
│       │   │   ├── port/
│       │   │   │   ├── inbound/                # CreateReservationPort, CancelReservationPort
│       │   │   │   └── outbound/               # ReservationRepository, PricingServicePort, SpotAvailabilityPort
│       │   │   └── service/                    # CancellationPolicyService (domain service)
│       │   ├── application/
│       │   │   ├── usecase/                    # CreateReservationUseCase, CancelReservationUseCase, GetReservationHistoryUseCase
│       │   │   ├── saga/                       # ReservationPaymentSaga (@Saga, tracking saga + deadline)
│       │   │   └── dto/                        # Commands, Queries, Responses
│       │   └── infrastructure/
│       │       ├── web/                        # ReservationController, RegistrationController
│       │       ├── persistence/                # JPA entities, OutboxEntry entity, ReservationJpaRepository
│       │       ├── messaging/                  # OutboxPoller (Kafka publisher), KafkaEventConsumer
│       │       ├── external/                   # PricingServiceClient (RestClient + Resilience4j CB)
│       │       ├── keycloak/                   # KeycloakRegistrationAdapter (Admin REST API)
│       │       └── config/                     # Axon config, Kafka config, Security (JWT resource server)
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/
│       └── test/java/com/parking/reservation/
│
├── payment-service/                            # Bounded Context: Payment Simulation
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/parking/payment/
│       │   ├── domain/
│       │   │   ├── model/                      # Payment (Axon @Aggregate), PaymentMethod VO
│       │   │   └── port/
│       │   │       ├── inbound/                # ProcessPaymentPort
│       │   │       └── outbound/               # PaymentRepository
│       │   ├── application/
│       │   │   ├── usecase/                    # ProcessPaymentUseCase (simulated async delay + random outcome)
│       │   │   └── dto/
│       │   └── infrastructure/
│       │       ├── web/                        # PaymentController (GET /methods)
│       │       ├── messaging/                  # KafkaPaymentRequestConsumer, KafkaPaymentResultPublisher
│       │       ├── persistence/                # JPA entities
│       │       └── config/
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/
│       └── test/java/com/parking/payment/
│
├── frontend/                                   # React + TypeScript SPA
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── src/
│       ├── pages/
│       │   ├── SearchPage.tsx
│       │   ├── ReservationPage.tsx
│       │   ├── ReservationHistoryPage.tsx
│       │   ├── AdminDashboardPage.tsx
│       │   └── RegistrationPage.tsx
│       ├── components/
│       │   ├── search/
│       │   ├── reservation/
│       │   ├── payment/
│       │   └── admin/
│       ├── services/                           # Fetch/Axios API clients per service domain
│       ├── hooks/                              # React Query hooks (useParkingSearch, useReservation…)
│       ├── auth/                               # oidc-client-ts integration (NO direct Keycloak calls)
│       └── types/                              # TypeScript interfaces mirroring API contracts
│   └── tests/                                  # Vitest + React Testing Library
│
├── infrastructure/
│   ├── keycloak/
│   │   └── realm-export.json                  # parking-realm: CUSTOMER + ADMINISTRATOR roles + seed user
│   ├── kafka/
│   │   └── init-topics.sh                     # Topic creation on broker startup
│   └── postgres/
│       └── init.sql                           # (Optional) per-service database creation
│
└── docs/
    ├── architecture/
    │   ├── c4-context.md
    │   └── c4-containers.md
    └── adr/
        └── 001-technology-choices.md
```

**Structure Decision**: Multi-module monorepo with a Maven parent POM for shared
dependency management. Each backend service is an independent Maven module with its own
Docker image build target. The frontend is a standalone Vite project. Infrastructure
configuration lives in `/infrastructure/` consumed by `docker-compose.yml`. No shared
Java library module is introduced in this feature (deferred until an actual cross-service
contract warrants it).

## Complexity Tracking

> No constitution violations. No complexity justification required.
