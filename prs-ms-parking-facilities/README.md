# Catalog Service

**Bounded Context**: Catalog & Availability

Manages the registry of parking facilities and their real-time availability.
It is the entry point for unauthenticated parking searches.

---

## Responsibilities

- Expose a full-text search API to find parking facilities by location and availability window.
- Maintain a Redis cache-aside layer for sub-100 ms availability queries on hot paths.
- Own the `ParkingFacility` and `ParkingSpot` aggregates.
- Invalidate cached availability when reservation events arrive via Kafka.

---

## Hexagonal Architecture

```
catalog/
├── domain/          # Pure business logic — no framework dependencies
│   ├── model/       # ParkingFacility (aggregate root), ParkingSpot, value objects, enums
│   ├── port/
│   │   ├── inbound/     # SearchParkingPort (use case interface)
│   │   └── outbound/    # FacilityRepository, AvailabilityCache
│   └── service/     # AvailabilityDomainService
├── application/     # Orchestrates domain objects; depends only on domain
│   ├── queries/     # SearchParkingQuery, SearchParkingQueryHandler
│   └── dto/         # FacilityResult, SearchRequest
├── infrastructure/  # Adapters; depends on application + domain
│   ├── jpa/         # ParkingFacilityJpaEntity, FacilityJpaRepository, mapper
│   ├── redis/       # RedisAvailabilityCache
│   └── config/      # Spring beans, security, SpringDoc
└── interfaces/      # Inbound adapters; depends only on application ports
    └── rest/        # CatalogController
```

**Layer dependency rule**: `domain ← application ← infrastructure / interfaces`
(arrows show "allowed to depend on"). Cross-layer violations are enforced by `ArchitectureTest`.

---

## REST Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/catalog/search` | None | Search parking facilities by text + availability window |
| `GET` | `/actuator/health` | None | Spring Boot Actuator health check |
| `GET` | `/v3/api-docs` | None | OpenAPI JSON descriptor |
| `GET` | `/swagger-ui.html` | None | Swagger UI |

### Search Parameters (`GET /api/v1/catalog/search`)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | Yes | Free-text search (name, city) |
| `checkIn` | ISO 8601 datetime | Yes | Entry datetime |
| `checkOut` | ISO 8601 datetime | Yes | Exit datetime |
| `page` | int | No (default 0) | Page number |
| `size` | int | No (default 20) | Page size |

---

## Kafka Events

### Consumed Topics

| Topic | Purpose |
|-------|---------|
| `parking.reservations` | Spot reservation events — triggers cache invalidation |

### Produced Topics

None (catalog is read-only for external consumers).

---

## Local Run

**Prerequisites**: Java 21, PostgreSQL 16, Redis 7, Kafka running locally or via Docker.

```bash
# Start only infrastructure dependencies
docker compose up postgres redis kafka -d

# Run the service
./gradlew :catalog-service:bootRun

# Verify health
curl http://localhost:8081/actuator/health
```

The service starts on port `8081`.
See [quickstart.md](../specs/001-system-modules-initial-structure/quickstart.md) for full stack setup.
