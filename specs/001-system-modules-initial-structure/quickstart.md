# Quickstart: Parking Reservation System — Local Development

**Date**: 2026-06-01
**Prerequisite tools**: `git`, `docker` ≥ 24.0, `docker compose` ≥ 2.20, `java` 21, `node` 20 LTS, `npm` 10+

> **Note**: No local Gradle installation is required. All backend builds use the committed
> `gradlew` wrapper.

---

## 1. Clone the Repository

```bash
git clone https://github.com/<org>/parking-reservation-system.git
cd parking-reservation-system
```

---

## 2. Configure Environment Variables

Copy the example env file and set required secrets:

```bash
cp .env.example .env
```

Edit `.env` and set at minimum:

```dotenv
# PostgreSQL (single instance, three databases)
POSTGRES_PASSWORD=parking

# Redis
REDIS_PASSWORD=redis_pass

# Keycloak
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_ADMIN_CLIENT_SECRET=change-me-before-production

# Grafana (optional)
GRAFANA_ADMIN_PASSWORD=admin
```

> **Never commit `.env` to version control.** The `.gitignore` already excludes it.

---

## 3. Start the Full Stack

```bash
docker compose up --build
```

This command starts the following containers (in dependency order):

| Service | Port | Notes |
|---------|------|-------|
| PostgreSQL | 5432 | Single instance — 3 databases: `catalog_db`, `reservation_db`, `payment_db` |
| Redis | 6379 | |
| Kafka (KRaft mode) | 9092 | No ZooKeeper; topics created by `kafka-init` container |
| Keycloak | 8080 | Realm auto-imported from `infrastructure/keycloak/realm-export.json` |
| catalog-service | 8081 | Spring Boot — fully implemented in iteration 001 |
| reservation-service | 8082 | Spring Boot — skeleton (full implementation in future iterations) |
| payment-service | 8083 | Spring Boot — skeleton (full implementation in future iterations) |
| frontend | 3000 | React + TypeScript SPA, served by Nginx |
| payment-gateway | 3001 | React + TypeScript mock payment gateway, served by Nginx |
| OTel Collector | 4317/4318 | OTLP gRPC/HTTP receivers |
| Prometheus | 9090 | Scrapes all backend `/actuator/prometheus` endpoints |
| Grafana | 3002 | Admin credentials set via `GRAFANA_ADMIN_PASSWORD` |

**First-run note**: Keycloak realm import and Flyway migrations run automatically. Allow
approximately 60–90 seconds for all services to reach `RUNNING` health status.

---

## 4. Verify Service Health

```bash
# All services should return { "status": "UP" }
curl http://localhost:8081/actuator/health | jq .
curl http://localhost:8082/actuator/health | jq .
curl http://localhost:8083/actuator/health | jq .
```

Expected output for each:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "kafka": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

---

## 5. Access the Application

| URL | Description |
|-----|-------------|
| `http://localhost:3000` | Main frontend SPA |
| `http://localhost:3001` | Payment gateway SPA (mock) |
| `http://localhost:8080` | Keycloak Admin Console |
| `http://localhost:8081/swagger-ui.html` | Catalog Service API docs (SpringDoc) |
| `http://localhost:8082/swagger-ui.html` | Reservation Service API docs (SpringDoc) |
| `http://localhost:8083/swagger-ui.html` | Payment Service API docs (SpringDoc) |

---

## 6. Keycloak Admin Credentials

| Field | Value |
|-------|-------|
| URL | `http://localhost:8080/admin` |
| Username | `admin` |
| Password | `admin` (development only — change in production) |
| Realm | `parking` |

Pre-configured roles: `CUSTOMER`, `ADMINISTRATOR`

---

## 7. Seed Data

Seed data is applied **automatically** on startup via Flyway migration `V2__seed_test_data.sql`.
No manual step is required.

The following test data is inserted into `catalog_db.catalog`:

- 3 parking facilities:
  - Madrid Centro (`a1b2c3d4-…`)
  - Barcelona Gràcia (`b2c3d4e5-…`)
  - Madrid Barajas Airport (`c3d4e5f6-…`)
- 5 parking spots per facility (types: STANDARD, COMPACT, DISABLED, EV)

> **Note**: Reservation and payment services are skeletons in iteration 001;
> administrator user creation is deferred to the reservation-service implementation.

---

## 8. Register a Customer Account

Using the frontend at `http://localhost:3000/register`, or via cURL:

```bash
curl -X POST http://localhost:8082/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "TestPass123!",
    "vehiclePlate": "1234XYZ",
    "requiresInvoice": false,
    "marketingConsent": false,
    "termsAccepted": true
  }' | jq .accessToken
```

Save the returned `accessToken` for the next steps.

---

## 9. Test the Search Flow

```bash
# Search for available parking (no auth required)
# Note: use ISO-8601 LOCAL date-time format (no Z or timezone offset) for checkIn/checkOut
curl "http://localhost:8081/api/v1/catalog/search?q=madrid&checkIn=2026-07-01T09:00:00&checkOut=2026-07-03T18:00:00" | jq .
```

---

## 10. Test the Reservation Flow

```bash
export ACCESS_TOKEN="<token from step 8>"
export FACILITY_ID="<facilityId from search response>"
export SPOT_ID="<spotId from search response>"

# Create reservation
curl -X POST http://localhost:8082/api/v1/reservations \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d "{
    \"facilityId\": \"$FACILITY_ID\",
    \"spotId\": \"$SPOT_ID\",
    \"checkIn\": \"2026-07-01T09:00:00Z\",
    \"checkOut\": \"2026-07-03T18:00:00Z\",
    \"paymentMethodCode\": \"VISA\",
    \"requiresInvoice\": false
  }" | jq .

# Poll for final status (wait ~3 seconds for payment simulation)
export RESERVATION_ID="<reservationId from above>"
sleep 3
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8082/api/v1/reservations/$RESERVATION_ID | jq .status
```

Expected final status: `"CONFIRMED"` (85% probability, configurable)

---

## 11. Run Tests

### Backend (all services)

```bash
./gradlew test
```

### Frontend

```bash
cd frontend
npm test
```

### Architecture enforcement (ArchUnit)

```bash
./gradlew :catalog-service:test :reservation-service:test :payment-service:test --tests "*ArchitectureTest"
```

---

## 12. Stop and Clean Up

```bash
# Stop all containers
docker compose down

# Stop and remove volumes (clears all database data)
docker compose down -v
```

---

## Prometheus & Observability

| URL | Description |
|-----|-------------|
| `http://localhost:9090` | Prometheus |
| `http://localhost:3002` | Grafana (admin credentials from `.env`) |
| `http://localhost:8081/actuator/prometheus` | Catalog raw metrics |
| `http://localhost:8082/actuator/prometheus` | Reservation raw metrics |
| `http://localhost:8083/actuator/prometheus` | Payment raw metrics |

---

## Common Issues

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `catalog-service` fails to start | PostgreSQL not ready | Wait 30 s; Flyway will retry |
| Keycloak returns 401 on register | Realm not imported | Check `docker compose logs keycloak`; restart if import failed |
| `SPOT_NOT_AVAILABLE` on reservation | Race condition with seed data | Run seed data script again after a clean restart |
| Frontend shows blank page | Nginx not ready | Wait 10–20 s after `docker compose up` |
| `catalog` schema missing / `parking_facility` relation not found | Flyway did not run | Ensure `spring-boot-flyway` is in catalog-service dependencies; verify `spring.flyway.enabled: true` in `application.yml` |
| All catalog API endpoints return 403 | Spring Security 7 path matching changed | `CatalogSecurityConfig` must use `anyRequest().permitAll()` for public catalog service (specific path matchers behave differently in Spring Security 7) |
| Frontend/payment-gateway nginx crash on startup | `upstream` hostname not resolvable at start | `nginx.conf` must use lazy DNS via `resolver 127.0.0.11` and `set $upstream` variable; static `proxy_pass http://hostname` causes nginx to fail if host is unreachable |
