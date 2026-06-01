# Reservation Service

**Bounded Context**: Reservations & SAGA

Manages the full lifecycle of a parking reservation — creation, payment orchestration
via SAGA, cancellation, and history. Enforces business invariants using Axon Framework
`@Aggregate` and `@Saga`.

---

## Responsibilities

- Accept `CreateReservationCommand` and `CancelReservationCommand` through its inbound ports.
- Orchestrate the payment flow using `ReservationPaymentSaga` (Axon tracking saga with deadline).
- Publish domain events to Kafka via the Transactional Outbox pattern.
- Validate JWT tokens issued by Keycloak (OAuth2 resource server).
- Register new users in Keycloak when they complete their first reservation.

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
