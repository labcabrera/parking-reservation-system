#!/bin/bash

COUNT=${1:-1}
CONCURRENCY=${2:-50}

CATALOG_URL="http://localhost:8081/api/v1/parking-facilities"
RESERVATIONS_URL="http://localhost:8081/api/v1/reservations"

# Fetch facilityId from the catalog service (first element)
echo "Fetching facility ID from catalog..."
FACILITY_ID=$(curl -s "${CATALOG_URL}?page=0&size=1" | jq -r '.content[0].id')

if [[ -z "$FACILITY_ID" || "$FACILITY_ID" == "null" ]]; then
  echo "ERROR: Could not retrieve facility ID from catalog. Is the service running?"
  exit 1
fi

echo "Using facility ID: ${FACILITY_ID}"
echo "Sending ${COUNT} requests with concurrency ${CONCURRENCY}..."

seq $COUNT | xargs -P $CONCURRENCY -I{} bash -c '
  UUID=$(uuidgen)
  BODY=$(printf "{\"facilityId\":\"%s\",\"checkIn\":\"2026-06-08T10:00:00\",\"checkOut\":\"2026-06-15T12:00:00\",\"bookingSessionId\":\"%s\"}" "$2" "$UUID")
  curl \
    -s \
    -o /dev/null \
    -w "%{http_code}\n" \
    -X POST "$1" \
    -H "Content-Type: application/json" \
    -d "$BODY"
' _ "$RESERVATIONS_URL" "$FACILITY_ID" | sort | uniq -c | sort -rn | awk '{printf "  HTTP %s : %s requests\n", $2, $1}'

echo "Total time: ${SECONDS}s"
