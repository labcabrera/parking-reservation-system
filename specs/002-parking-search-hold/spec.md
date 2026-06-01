# Feature Specification: Parking Search & Reservation Start (Shopping Session)

**Feature Branch**: `002-parking-search-hold`
**Created**: 2026-06-01
**Status**: Draft
**Input**: `docs/shoping-session-instructions.md` — Shopping Session decisions and design analysis

---

## Clarifications

### Session 2026-06-01 (continued)

- Q: Should SC-001 (search p95 latency) be 100 ms (aligned with constitution §V) or 500 ms? → A: 100 ms. The Redis cache-aside path MUST cover nearly all search requests under normal load; a DB hit is the exceptional path (cache miss or stale). The search endpoint MUST be designed so the cache-hit path is the common case.
- Q: How should SC-002 (hold creation latency) be measured given the async two-step flow? → A: Split into two sub-criteria — the `202 Accepted` response MUST be returned at p95 ≤ 300 ms (synchronous path); the hold MUST reach `ACTIVE` status within 3 s of creation at p95 (end-to-end async Kafka round-trip).
- Q: Should `PENDING_PRICE` and `FAILED` be canonical SpotHold states visible to API consumers? → A: Yes. Both states are surfaced in `GET /holds/{holdId}` polling responses; clients need them to decide whether to continue polling (`PENDING_PRICE`) or surface an error to the user (`FAILED`). The spec state machine is extended from 4 to 6 states.
- Q: Are FR-004 (paginated results) and FR-005 (filter by facility characteristics) in scope for this iteration? → A: Both in scope. FR-004 pagination and FR-005 characteristic filters (covered, EV charging, free cancellation) are confirmed MUST requirements for this feature. Tasks will be added to tasks.md.
- Q: The Assumptions section named `catalog-service` as requiring Axon migration — which service is the actual prerequisite? → A: `reservation-service` (currently on Axon 4.10.3). `catalog-service` has never used Axon and requires no migration. Assumption corrected.

### Session 2026-06-01

- Q: Does hold creation require authentication or can it be anonymous? → A: Anonymous hold creation is allowed; rate-limited per IP and `searchSessionId` to prevent abuse. A `userId` is optional on a hold and is populated only when the visitor is authenticated at checkout time.
- Q: What is the complete state machine for a Spot Hold? → A: Four states — `ACTIVE` (hold created, spot locked), `EXPIRED` (TTL elapsed, auto-released), `RELEASED` (explicitly freed when visitor selects a different parking within the same session), `CONVERTED` (visitor has advanced to reservation; hold is superseded).
- Q: How is the location field resolved to matching parking facilities? → A: Free-text query against facility name, city, and tags; optionally combined with geographic coordinates (`lat`/`lng`/`radiusKm`) for proximity filtering. No external geocoding service is involved.
- Q: What should the search endpoint return when the persistence layer is unavailable? → A: Return cached results with a `stale: true` indicator in the response; do not return 503 unless the cache is also unavailable.
- Q: Is there a minimum advance booking time for search or hold creation? → A: No minimum advance time. The only temporal constraint is `checkOut > checkIn`. Per-facility restrictions (e.g. minimum 2 hours notice) are a future data-model concern, not enforced at search or hold level.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Visitor searches for available parking (Priority: P1)

An unauthenticated visitor enters a location, check-in date/time, check-out date/time and
optional feature filters (e.g. covered, EV charging) on the search form. The system
instantly returns a list of matching parking facilities with an estimated price for the
requested period.

**Why this priority**: Search is the entry point of the entire reservation journey.
Every subsequent step depends on a working, performant search. Without it, no reservation
can be started.

**Independent Test**: Submit a search request with a valid location and date range. The
system returns a non-empty list of facilities within acceptable latency. Each result
includes name, location, availability indicator, estimated price, and a session identifier.

**Acceptance Scenarios**:

1. **Given** the system has seeded parking data, **When** a visitor submits a search with
   a free-text location query (matching facility name, city, or tag) and a valid date
   range, **Then** the system returns a paginated list of matching facilities, each
   showing name, address, estimated total price, available spots indicator, applicable
   tags, and a unique search session identifier.
2. **Given** a search is submitted, **When** the results are returned, **Then** a unique
   `searchSessionId` is included in the response to track subsequent actions in this
   shopping session.
3. **Given** a search with no matching facilities, **When** the visitor submits the
   form, **Then** the system returns an empty list with an appropriate message, not an
   error.
4. **Given** a facility has fewer than the configured low-availability threshold of its
   spots available, **When** it appears in results, **Then** it is marked with a
   low-availability warning.
5. **Given** the system is under load with many concurrent searches, **When** a visitor
   submits a search, **Then** the response is returned within the target latency with no
   errors.

---

### User Story 2 — Visitor selects a parking and obtains a confirmed price hold (Priority: P2)

After reviewing search results, the visitor selects one parking option using the
`searchSessionId` from the previous search. The system creates a temporary hold on a spot
in that facility, calculates the confirmed price, and returns both to the visitor along
with a hold expiry timestamp. The visitor can then proceed to the reservation and payment
steps.

**Why this priority**: The hold with confirmed price is the bridge between browsing and
booking. It prevents overselling, guarantees the price for a limited window, and gives
the user confidence to proceed to payment.

**Independent Test**: Using the `searchSessionId` from a search, select one of the
returned facilities. The system responds with a hold confirmation containing a confirmed
price, a spot reference, and an expiry timestamp 10 minutes in the future.

**Acceptance Scenarios**:

1. **Given** a visitor has received search results with a valid `searchSessionId`,
   **When** the visitor selects a parking facility, **Then** the system creates a hold
   on an available spot and returns a confirmed price, spot identifier, and hold expiry
   time.
2. **Given** a hold is created, **When** the visitor views the hold details, **Then**
   the confirmed price reflects the specific period and facility characteristics, not just
   a base-rate approximation.
3. **Given** a visitor already has an active hold within the same search session,
   **When** they select a different parking from the same results, **Then** the previous
   hold is released automatically and a new hold is created for the newly selected
   facility.
4. **Given** no spots are available in the selected facility for the requested period,
   **When** the visitor selects it, **Then** the system informs the visitor that no spots
   are available and no hold is created.

---

### User Story 3 — Hold expires automatically if visitor does not proceed (Priority: P3)

If a visitor creates a hold but does not proceed to complete the reservation within the
hold validity window, the system automatically releases the held spot, making it available
to other visitors again.

**Why this priority**: Automatic expiry is essential to prevent inventory lock-up.
Without it, abandoned sessions would block spots indefinitely, degrading availability
for all users.

**Independent Test**: Create a hold and wait for the configured TTL to elapse without
completing the reservation. Verify the spot is available again and the hold is marked
as expired.

**Acceptance Scenarios**:

1. **Given** a hold is active, **When** the hold TTL elapses without the visitor
   confirming the reservation, **Then** the held spot is automatically released back to
   available inventory.
2. **Given** a hold has expired, **When** the visitor attempts to continue the
   reservation using the expired hold, **Then** the system informs the visitor the hold
   has expired and prompts them to start a new selection.

---

### Edge Cases

- What happens when the selected facility has spots available but all are temporarily
  held by other users? The system must inform the visitor that no spots are currently
  available and must not create a hold.
- What happens if a visitor submits a search with a check-out date/time before or equal
  to the check-in date/time? The system must reject the request with a clear validation
  message; no search is performed.
- What happens if the price calculation service is unavailable when a hold is created?
  The hold must not be created; the visitor must receive an error message indicating the
  service is temporarily unavailable.
- What happens if a visitor's search results become stale while they are browsing? A
  stale `searchSessionId` remains usable for initiating a hold, but the system must
  re-validate spot availability at hold-creation time.
- What happens when two visitors simultaneously attempt to hold the last available spot
  in a facility? Exactly one hold must succeed; the other must receive an appropriate
  unavailability response.
- What happens when the persistence layer is unavailable during a search? The system must
  return cached results with a `stale: true` flag in the response. If neither persistence
  nor cache is available, the system must return a 503 with a clear service-unavailable
  message.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST allow any visitor (authenticated or not) to search for
  parking facilities by providing a free-text query (matched against facility name, city,
  and tags) and a date/time range, without requiring prior authentication.
- **FR-019**: The system MUST support an optional proximity filter using geographic
  coordinates (`latitude`, `longitude`, and `radiusKm`); when provided alongside the
  free-text query, results MUST be further restricted to facilities within the specified
  radius. No external geocoding service is required.
- **FR-020**: When the primary persistence store is unavailable, the search endpoint MUST
  return results from cache with a `stale: true` indicator in the response body. If both
  persistence and cache are unavailable, the endpoint MUST return a 503 error.
- **FR-002**: Search results MUST include an estimated total price per facility for the
  requested period.
- **FR-003**: The system MUST return a unique `searchSessionId` with each search response
  to correlate the search with subsequent hold and reservation actions.
- **FR-004**: Search results MUST be paginated; the maximum page size MUST be configurable
  via a system parameter.
- **FR-005**: Visitors MUST be able to filter search results by desired facility
  characteristics (e.g. covered, EV charging, free cancellation).
- **FR-006**: Facilities with available spots below a configurable low-availability
  threshold MUST be flagged with a low-availability warning in search results.
- **FR-007**: Any visitor (authenticated or not) MUST be able to select a parking
  facility from their search results, using the `searchSessionId`, to initiate a hold;
  no authentication is required at this step.
- **FR-008**: When a visitor selects a parking facility, the system MUST create a hold
  on one available spot in that facility for the requested period.
- **FR-017**: The system MUST apply rate limiting on hold creation requests per IP address
  and per `searchSessionId` to prevent abuse of the anonymous hold endpoint.
- **FR-018**: A hold MUST transition to `CONVERTED` state when the visitor advances to a
  reservation using that hold; a `CONVERTED` hold MUST NOT be reusable or cancellable
  via the hold endpoint.
- **FR-009**: A hold MUST have a time-to-live (TTL); the default value is 10 minutes and
  MUST be configurable via a system parameter.
- **FR-010**: After a hold is created, the system MUST calculate and return a confirmed
  price for that specific hold.
- **FR-011**: If a visitor already has an active hold within the same search session,
  the system MUST release the previous hold automatically when a new one is created.
- **FR-012**: A hold MUST be automatically released when its TTL expires, restoring the
  spot to available inventory.
- **FR-013**: If no available spots exist in the selected facility for the requested
  period, the system MUST return an appropriate error and MUST NOT create a hold.
- **FR-014**: The system MUST prevent two visitors from holding the same spot for the
  same period simultaneously (no double-hold).
- **FR-015**: The system MUST provide a real-time stream endpoint that notifies connected
  clients of changes in spot availability for a given facility.
- **FR-016**: Search and hold requests with invalid date ranges (check-out ≤ check-in)
  MUST be rejected with a descriptive validation error before any processing occurs. No
  minimum advance booking time is enforced; check-in may be in the past or present as
  long as check-out is strictly after check-in.

### Key Entities

- **Search Session**: Represents a single search interaction. Contains the search
  parameters (location, dates, filters), the result set, and the `searchSessionId`. Does
  not expire; requires no authentication.
- **Parking Facility**: A physical parking location with a name, address, geographic
  coordinates, available spot count, feature tags, and a base pricing rate per day.
- **Spot Hold**: A temporary reservation of a specific spot within a facility, tied to a
  `searchSessionId`. Has an expiry timestamp and a confirmed price. Six-state machine
  (all states are canonical and visible to API consumers via `GET /holds/{holdId}`):
  - `PENDING_PRICE`: hold created and spot locked; awaiting confirmed price from pricing
    service via async Kafka round-trip. Client MUST keep polling.
  - `ACTIVE`: confirmed price received; hold is fully active and spot is locked.
  - `EXPIRED`: TTL elapsed without visitor action; spot automatically returned to inventory.
  - `RELEASED`: explicitly freed when the visitor selects a different parking within the
    same search session; spot immediately returned to inventory.
  - `CONVERTED`: visitor has advanced to a reservation using this hold; hold is superseded
    and the spot transitions to reserved under the new reservation.
  - `FAILED`: pricing service could not confirm a price within the timeout window; hold
    is void and spot is returned to inventory. Client MUST NOT retry the same hold.
  The `userId` field is optional — it is null for anonymous holds and populated when the
  visitor authenticates at checkout time.
- **Estimated Price**: A price calculated at search time using the facility's base daily
  rate and the number of requested days. Indicative only — not a binding commitment.
- **Confirmed Price**: A price calculated and locked at hold-creation time by the pricing
  service. Binding for the duration of the hold.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A search request returns results in under **100 milliseconds** at the 95th
  percentile under normal operating load. The Redis cache-aside path MUST be the common
  case; a direct DB hit is the exceptional path (cache miss or first-load). This aligns
  with constitution §V.
- **SC-002a**: The `POST /holds` endpoint returns a `202 Accepted` response at the 95th
  percentile in under **300 milliseconds** (synchronous path only — no pricing computation).
- **SC-002b**: A hold reaches `ACTIVE` status (confirmed price available) within **3 seconds**
  of creation at the 95th percentile, covering the full async Kafka pricing round-trip.
- **SC-003**: 100% of holds whose TTL has elapsed are released within 30 seconds of
  expiry under normal operating conditions.
- **SC-004**: Visitors can complete the full journey from search to hold without
  encountering errors in at least 99% of attempts when inventory is available and the
  primary persistence layer is healthy. When the persistence layer is degraded, search
  results from cache are served with a `stale: true` flag and hold creation remains
  operational.
- **SC-005**: Concurrent hold attempts on the same spot for the same period result in
  exactly one successful hold and appropriate rejection responses for the rest — zero
  double-holds.
- **SC-006**: The real-time availability stream reflects spot availability changes within
  5 seconds of the change occurring.

---

## Assumptions

- The `catalog-service` acts as the discovery endpoint for search; no new service is
  required for this role.
- Estimated price at search time is calculated by the `catalog-service` using the
  facility's base daily rate × number of days. It is an approximation and may differ
  slightly from the confirmed price.
- Confirmed price is calculated by a dedicated `pricing-service` invoked after hold
  creation; the pricing factors include the number of days, facility type, and spot
  availability at the time of the hold.
- Hold TTL defaults to 10 minutes, configurable; a visitor can have at most one active
  hold per search session.
- Both search and hold creation are publicly accessible without authentication. Rate
  limiting per IP and `searchSessionId` prevents abuse of the anonymous hold endpoint.
  The `userId` on a hold is nullable; it is populated when the visitor authenticates at
  checkout time (handled in the reservation flow spec).
- No minimum advance booking time is enforced at the search or hold level. The only
  temporal validation is `checkOut > checkIn`. Per-facility advance-notice rules are out
  of scope for this iteration.
- The `searchSessionId` is an opaque identifier generated by the backend; the client
  stores it and forwards it in subsequent requests but does not interpret its contents.
- The following are out of scope for this iteration: payment processing, full reservation
  confirmation, reservation cancellation, user profile management, and administrator
  operations. These are covered in subsequent specs.
- Location resolution uses free-text matching against facility name, city, and tags,
  optionally combined with geographic coordinates for proximity filtering. No external
  geocoding API dependency is introduced in this iteration.
- The `pricing-service` is a new backend module to be added to the monorepo; its internal
  pricing logic and Kafka integration are defined in its own spec.
- All services in this feature will use **Axon 5.x**. The `reservation-service` migration
  from Axon 4.10.3 to 5.x is a prerequisite task included in this spec's implementation
  plan. `catalog-service` does not use Axon and requires no Axon migration.
