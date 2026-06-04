# Payment Service

**Bounded Context**: Payment Simulation

Simulates external payment processing. Receives payment requests from the Reservation Service
via Kafka, delegates the actual payment UI to the `payment-gateway` SPA, and publishes
success/failure results back to the SAGA.

---

## Responsibilities

- Consume `parking.payments.requests` Kafka topic and initiate a payment session.
- Redirect the user to the `payment-gateway` SPA for card entry simulation.
- Process the payment result and publish to `parking.payments.results`.
- Own the `Payment` aggregate.

---

## Hexagonal Architecture

```
payment/
├── domain/
│   ├── model/       # Payment (@Aggregate), PaymentMethod enum, PaymentStatus enum
│   ├── port/
│   │   ├── inbound/     # ProcessPaymentPort, GetPaymentPort
│   │   └── outbound/    # PaymentRepository, PaymentGatewayPort
│   └── service/     # (no domain service in initial scope)
├── application/
│   ├── commands/    # ProcessPaymentCommand, ConfirmPaymentCommand + handlers
│   ├── queries/     # GetPaymentQuery + handler
│   └── dto/         # PaymentRequest, PaymentResult
├── infrastructure/
│   ├── persistence/ # JPA entities, PaymentJpaRepository
│   ├── messaging/   # PaymentRequestConsumer (Kafka inbound), PaymentResultPublisher (outbound)
│   └── config/      # Axon config, SpringDoc config
└── interfaces/
    ├── rest/        # PaymentController (redirect to payment-gateway)
    └── messaging/   # PaymentRequestListener
```

---

## Kafka Topics

| Topic | Direction | Purpose |
|-------|-----------|---------|
| `parking.payments.requests` | Consumed | Payment request from reservation SAGA |
| `parking.payments.results` | Produced | Payment confirmed/failed event back to SAGA |

---

## Payment Flow

```
[Reservation SAGA] → parking.payments.requests
  → PaymentRequestListener
    → ProcessPaymentCommand
      → Payment aggregate created (PENDING)
        → Redirect URL generated pointing to payment-gateway SPA
          → User completes/cancels on payment-gateway SPA
            → POST /api/v1/payments/{id}/confirm or /cancel
              → Payment aggregate transitions to CONFIRMED / FAILED
                → parking.payments.results published
```

---

## REST Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/payments/{id}` | Internal | Get payment status |
| `POST` | `/api/v1/payments/{id}/confirm` | Internal (from gateway SPA) | Confirm payment |
| `POST` | `/api/v1/payments/{id}/cancel` | Internal (from gateway SPA) | Cancel payment |
| `GET` | `/actuator/health` | None | Health check |

---

## Local Run

```bash
docker compose up postgres kafka -d

./gradlew :payment-service:bootRun

curl http://localhost:8083/actuator/health
```

The service starts on port `8083`.
