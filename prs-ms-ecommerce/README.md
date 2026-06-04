# Reservation Service

**Bounded Context**: Parking Spot Hold & Reservation

Manages the lifecycle of parking spot holds — creation, pricing confirmation via async Kafka flow,
expiry, release, and conversion to full reservations. Uses Axon Framework 5 `@EventSourcedEntity`
for the `SpotHold` aggregate.

---

## Responsibilities

- Accept `CreateHoldCommand`, `ConfirmHoldPriceCommand`, `ReleaseHoldCommand`, `ExpireHoldCommand`,
  `ConvertHoldCommand` through REST and Kafka adapters.
- Coordinate async pricing via `HoldPricingCoordinator` (Kafka round-trip to `pricing-service`).
- Publish availability changes to `parking.availability.changes` on hold lifecycle events.
- Apply per-IP and per-session rate limiting (Resilience4j) on hold creation.
- Validate JWT tokens issued by Keycloak (OAuth2 resource server).

---

## Hexagonal Architecture

```
reservation/
├── domain/
│   ├── model/         # SpotHold (@EventSourcedEntity), HoldStatus, Money, HoldReadModel
│   ├── port/
│   │   ├── inbound/   # (commands dispatched via CommandGateway)
│   │   └── outbound/  # HoldRepository, PricingRequestPort
│   └── model/events/  # HoldCreatedEvent, HoldPriceConfirmedEvent, ...
├── application/
│   ├── commands/      # CreateHoldCommand, ConfirmHoldPriceCommand, etc.
│   ├── SpotHoldCommandHandler.java  (Axon @CommandHandler beans)
│   └── HoldPricingCoordinator.java  (coordinates pricing Kafka round-trip)
├── infrastructure/
│   ├── config/        # ResilienceConfig (Resilience4j RateLimiter + CircuitBreaker)
│   ├── messaging/     # PricingRequestPublisher, PricingResultConsumer, AvailabilityChangePublisher
│   ├── metrics/       # HoldMetricsListener (Micrometer counters)
│   ├── persistence/   # SpotHoldJpaEntity (Flyway + PostgreSQL)
│   └── scheduling/    # HoldExpiryScheduler (fallback TTL sweep)
└── interfaces/rest/   # HoldController (POST /api/v1/reservations/holds)
```

---

## Configuration Properties

| Property | Default | Description |
|---|---|---|
| `reservation.hold.ttl-seconds` | `600` | Hold TTL in seconds (10 min) |
| `reservation.hold.pricing-timeout-seconds` | `30` | Max wait for pricing result |

---

## Kafka Topics

| Topic | Direction | Description |
|---|---|---|
| `parking.pricing.requests` | Producer | Send pricing request to pricing-service |
| `parking.pricing.results` | Consumer | Receive pricing result from pricing-service |
| `parking.availability.changes` | Producer | Notify catalog-service of spot availability changes |

---

## REST Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/reservations/holds` | Create hold — 202 Accepted, pricing async |
| `GET` | `/api/v1/reservations/holds/{holdId}` | Get hold status — 200 or 404 |
| `DELETE` | `/api/v1/reservations/holds/{holdId}` | Release hold — 204 or 409 Conflict |

---

## Quickstart

```bash
# Start infrastructure
docker compose -f docker-compose-infra.yaml up -d

# Run the service
./gradlew :reservation-service:bootRun

# Create a hold
curl -X POST http://localhost:8082/api/v1/reservations/holds \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt>" \
  -d '{"searchSessionId":"sess-1","spotId":"<uuid>","facilityId":"<uuid>","checkIn":"2026-07-01T10:00:00Z","checkOut":"2026-07-03T10:00:00Z","estimatedPriceAmount":50.00,"currency":"EUR"}'

# Poll for ACTIVE status (up to ~30s while pricing-service processes)
curl http://localhost:8082/api/v1/reservations/holds/<holdId>
```


---

## Hexagonal Architecture

```
reservation/
├── domain/
│   ├── model/       # Reservation (@Aggregate), PriceLock VO, ReservationPeriod VO, Money VO
│   ├── port/
│   │   ├── inbound/     # CreateReservationPort, CancelReservationPort
│   │   └── outbound/    # ReservationRepository, PricingServicePort, SpotAvailabilityPort
│   └── service/     # CancellationPolicyService (domain service)
├── application/
│   ├── commands/    # CreateReservationCommand, CancelReservationCommand + handlers
│   ├── queries/     # GetReservationQuery, GetReservationHistoryQuery + handlers
│   ├── saga/        # ReservationPaymentSaga (@Saga, deadline management)
│   └── dto/         # Response DTOs
├── infrastructure/
│   ├── persistence/ # JPA entities, OutboxEntry, ReservationJpaRepository
│   ├── messaging/   # OutboxPoller (Kafka publisher)
│   ├── external/    # PricingServiceClient (RestClient + Resilience4j CB)
│   ├── keycloak/    # KeycloakUserRegistrationAdapter
│   └── config/      # Security config, Resilience4j config, SpringDoc
└── interfaces/
    ├── rest/        # ReservationController
    └── messaging/   # ReservationEventListener (Kafka inbound)
```

---

## SAGA Orchestration: `ReservationPaymentSaga`

```
CreateReservationCommand
  → Reservation.@CommandHandler
  → ReservationCreatedEvent
    → ReservationPaymentSaga starts
      → SendPaymentRequestCommand (timeout deadline set: 15 min)
        → payment-service processes
          → PaymentConfirmedEvent / PaymentFailedEvent
            → Saga completes reservation or compensates (releases spot)
      → DeadlineExceededEvent (if no response in 15 min)
        → CancelReservationCommand (compensation)
```

---

## Kafka Topics

| Topic | Direction | Purpose |
|-------|-----------|---------|
| `parking.payments.requests` | Produced | Payment request sent by SAGA |
| `parking.payments.results` | Consumed | Payment confirmation/failure from payment-service |
| `parking.reservations` | Produced | Domain events (created, confirmed, cancelled) |

---

## REST Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/reservations` | JWT (CUSTOMER) | Create a reservation |
| `DELETE` | `/api/v1/reservations/{id}` | JWT (CUSTOMER) | Cancel a reservation |
| `GET` | `/api/v1/reservations/{id}` | JWT (CUSTOMER/ADMIN) | Get reservation details |
| `GET` | `/api/v1/reservations` | JWT (CUSTOMER) | List my reservations |
| `GET` | `/actuator/health` | None | Health check |

---

## Local Run

```bash
docker compose up postgres kafka keycloak -d

./gradlew :reservation-service:bootRun

curl http://localhost:8082/actuator/health
```

The service starts on port `8082`.
