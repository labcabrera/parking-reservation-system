# ADR 001: Technology Choices

**Status**: Accepted  
**Date**: 2026-06-01  
**Deciders**: Architecture Team

## Context

We are building a parking reservation system as a set of independently deployable microservices with a web frontend. The system must support eventual consistency between bounded contexts, a secure user authentication model, and high availability.

## Decision

### Backend

| Concern | Decision | Rationale |
|---------|----------|-----------|
| Language & Runtime | Java 21 | Long-term support (LTS), virtual threads (Project Loom), modern language features (records, sealed classes) |
| Framework | Spring Boot 4.0.6 | De-facto standard for JVM microservices; native support for JPA, Security, Kafka, Actuator, and OpenAPI |
| Build Tool | Gradle 8.13 (Groovy DSL) | Multi-project support, fast incremental builds, flexible BOM import |
| Architecture Pattern | 4-Layer Hexagonal (domain / application / infrastructure / interfaces) | Enforces dependency rule; domain is completely isolated from infrastructure |
| Command Handling | Axon Framework 4.10 (no AxonServer, no Event Sourcing) | Provides `@Aggregate`, `@Saga`, `@DeadlineManager`; runs with local command bus |
| Persistence | PostgreSQL 16 + JPA/Hibernate 6 | ACID transactions, `@Version` optimistic locking on hot entities |
| Schema Migration | Flyway | Version-controlled SQL migrations; simple to operate |
| Cache | Redis 7 (cache-aside) | Sub-millisecond read path for availability index; TTL-based expiry |
| Messaging | Apache Kafka | Decouples services; durable, partitioned topic log |
| Outbox Pattern | Custom `outbox_events` table + `OutboxPoller` | At-least-once Kafka delivery without distributed transactions |
| Identity & Access | Keycloak 24 (OAuth2/OIDC JWT) | Open-source; supports custom realms, role-based access, Admin REST API |
| API Documentation | SpringDoc OpenAPI 2.x | Code-first; integrates with Spring MVC and Spring Security |
| Resilience | Resilience4j 2 | Circuit Breaker, Rate Limiter — lightweight, Spring Boot 3+ compatible |
| Observability | OpenTelemetry Java Agent | Vendor-neutral distributed tracing; structured JSON logs with `traceId`/`spanId` |
| Architecture Testing | ArchUnit 1.x | Enforces layer isolation at CI time |
| Integration Testing | Testcontainers | Ephemeral Docker containers for PostgreSQL, Redis, Kafka in tests |

### Frontend

| Concern | Decision | Rationale |
|---------|----------|-----------|
| Framework | React 18 | Component model, wide ecosystem |
| Language | TypeScript 5 | Type safety, better IDE support |
| Build Tool | Vite 5 | Fast HMR, native ESM, minimal config |
| Routing | React Router 6 | Declarative routing |
| Server State | TanStack Query v5 | Caching, background refetch, mutation handling |
| Auth | oidc-client-ts | OIDC PKCE flow in browser |

### Infrastructure

| Concern | Decision | Rationale |
|---------|----------|-----------|
| Container Runtime | Docker + Docker Compose | Local development parity |
| Service Ports | 8081 catalog, 8082 reservation, 8083 payment, 3000 frontend, 3001 payment-gateway | Predictable, non-overlapping |

## Consequences

- Axon's local command bus simplifies deployment (no AxonServer dependency) but limits event sourcing capabilities — accepted for now.
- Outbox pattern adds implementation complexity but eliminates dual-write risk.
- Redis TTL-based cache requires explicit invalidation logic on write paths.
- Keycloak Admin REST API for user registration couples the reservation service to Keycloak — mitigated by wrapping behind a port/adapter.
