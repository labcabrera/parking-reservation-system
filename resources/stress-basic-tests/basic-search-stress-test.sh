#!/bin/bash

COUNT=${1:-1000}
CONCURRENCY=${2:-50}

FACILITY_ID="790fee9f-1c34-4fd2-9046-050cee6d7978"
URL="http://localhost:8081/api/v1/reservations"

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
' _ "$URL" "$FACILITY_ID" | sort | uniq -c | sort -rn | awk '{printf "  HTTP %s : %s requests\n", $2, $1}'
