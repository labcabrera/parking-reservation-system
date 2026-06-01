# Contract: Registration & Authentication API

**Service**: `reservation-service` (hosts user-facing auth endpoints)
**Base URL**: `http://reservation-service/api/v1/auth`
**Authentication**: Public (no token required)
**Date**: 2026-06-01

---

## Overview

User registration and token acquisition are handled by the backend, which calls the
Keycloak Admin REST API internally. The frontend MUST NEVER call Keycloak Admin endpoints
directly. The Keycloak admin client credentials are server-side-only environment variables.

---

## Endpoints

### `POST /register` — Register New Customer

Creates a new `CUSTOMER` account in Keycloak and the corresponding `UserProfile` in the
`reservation-service` database.

**Authentication**: None (public)

**Request Body** (`application/json`):

```json
{
  "fullName": "Ana García López",
  "email": "ana.garcia@example.com",
  "password": "SuperSecret123!",
  "vehiclePlate": "1234ABC",
  "requiresInvoice": false,
  "marketingConsent": false,
  "termsAccepted": true
}
```

**Field validation rules**:

| Field | Rules |
|-------|-------|
| `fullName` | Required, 2–100 chars |
| `email` | Required, valid RFC 5322 email, unique in Keycloak realm |
| `password` | Required, min 10 chars, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char |
| `vehiclePlate` | Required, 1–20 chars, alphanumeric + hyphens |
| `requiresInvoice` | Required boolean |
| `marketingConsent` | Required boolean |
| `termsAccepted` | Required, MUST be `true` |

**Response `201 Created`**:

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "8e6b3b1a-4c8b-4c8b-b3b1-4c8b4c8b4c8b"
}
```

The `accessToken` is the Keycloak-issued JWT with `CUSTOMER` role. The frontend SHOULD
store it in memory (NOT `localStorage` to reduce XSS surface) and use it for subsequent
authenticated requests.

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `400 Bad Request` | `VALIDATION_ERROR` | Field validation failure — see `violations` array |
| `400 Bad Request` | `TERMS_NOT_ACCEPTED` | `termsAccepted` is `false` |
| `409 Conflict` | `EMAIL_ALREADY_REGISTERED` | Email already exists in the Keycloak realm |
| `429 Too Many Requests` | `RATE_LIMIT_EXCEEDED` | > 10 registration attempts per IP per minute |
| `503 Service Unavailable` | `IDENTITY_PROVIDER_UNAVAILABLE` | Keycloak unreachable |

**Validation error body example**:

```json
{
  "timestamp": "2026-06-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Request contains invalid fields",
  "violations": [
    {
      "field": "password",
      "message": "must contain at least one special character"
    },
    {
      "field": "email",
      "message": "must be a valid email address"
    }
  ],
  "path": "/api/v1/auth/register",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736"
}
```

---

### `POST /token` — Obtain Token (Login)

Exchanges email + password for a Keycloak access token. Acts as a thin proxy to
Keycloak's token endpoint; credentials are forwarded to Keycloak's Resource Owner
Password Credentials flow (server-side only). The Keycloak client secret is never
exposed to the browser.

> **Note**: In a production deployment, the frontend SHOULD use the standard PKCE
> Authorization Code Flow via `oidc-client-ts`. This endpoint exists as a convenience
> for the MVP and integration tests.

**Authentication**: None (public)

**Request Body** (`application/json`):

```json
{
  "email": "ana.garcia@example.com",
  "password": "SuperSecret123!"
}
```

**Response `200 OK`**:

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error responses**:

| Status | Code | Condition |
|--------|------|-----------|
| `401 Unauthorized` | `INVALID_CREDENTIALS` | Email or password incorrect |
| `429 Too Many Requests` | `RATE_LIMIT_EXCEEDED` | > 10 login attempts per IP per minute |
| `503 Service Unavailable` | `IDENTITY_PROVIDER_UNAVAILABLE` | Keycloak unreachable |

---

## Security Notes

1. **Password is never logged or stored.** It is forwarded directly to Keycloak's token
   endpoint over HTTPS and then discarded.
2. **JWT validation**: All downstream services validate the JWT signature using Keycloak's
   JWKS endpoint (`keycloak:8080/realms/parking/protocol/openid-connect/certs`). No
   introspection endpoint is used.
3. **Token lifetime**: Access token TTL is 3600 s (1 hour). Refresh token TTL is 86400 s
   (24 hours), both configured in `realm-export.json`.
4. **Rate limiting**: `RateLimiter` from Resilience4j applied at the controller level.
5. **Sensitive PII** (`fullName`, `vehiclePlate`) is encrypted at rest in `UserProfile`
   and MUST NOT appear in any log, metric tag, or event payload.
