#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="$SCRIPT_DIR/json"
PRICING_RULE_DIR="$DATA_DIR/pricing-rules"
FACILITIES_DIR="$DATA_DIR/parking-facilities"

PRICING_BASE_URL="${PRICING_BASE_URL:-http://localhost:8083}"
FACILITIES_BASE_URL="${FACILITIES_BASE_URL:-http://localhost:8081}"

AUTH_HEADERS=()
if [[ -n "${AUTH_TOKEN:-}" ]]; then
  AUTH_HEADERS=(-H "Authorization: Bearer $AUTH_TOKEN")
fi

cleanup_files=()
cleanup() {
  for file in "${cleanup_files[@]}"; do
    [[ -f "$file" ]] && rm -f "$file"
  done
}
trap cleanup EXIT

post_json() {
  local url="$1"
  local file="$2"
  local response_file
  local status

  response_file="$(mktemp)"
  cleanup_files+=("$response_file")

  status="$(
    curl -sS \
      -o "$response_file" \
      -w "%{http_code}" \
      -H "Content-Type: application/json" \
      "${AUTH_HEADERS[@]}" \
      -X POST \
      --data-binary "@$file" \
      "$url"
  )"

  if [[ "$status" -lt 200 || "$status" -ge 300 ]]; then
    echo "Error POST $url ($status)"
    cat "$response_file"
    echo
    exit 1
  fi

  cat "$response_file"
}

extract_json_id() {
  tr -d '\n' | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
}

pricing_rule_id=""
for file in "$PRICING_RULE_DIR"/*.json; do
  [[ -e "$file" ]] || continue

  echo "Creating pricing rule from $file"
  response="$(post_json "$PRICING_BASE_URL/api/v1/pricing-rules" "$file")"
  pricing_rule_id="$(printf '%s' "$response" | extract_json_id)"

  if [[ -z "$pricing_rule_id" ]]; then
    echo "Could not extract pricing rule id from response:"
    printf '%s\n' "$response"
    exit 1
  fi

  echo "Pricing rule created: $pricing_rule_id"
  break
done

if [[ -z "$pricing_rule_id" ]]; then
  echo "No pricing rule JSON files found in $PRICING_RULE_DIR"
  exit 1
fi

for file in "$FACILITIES_DIR"/*.json; do
  [[ -e "$file" ]] || continue

  tmp_file="$(mktemp)"
  cleanup_files+=("$tmp_file")
  sed "s/__PRICING_RULE_ID__/$pricing_rule_id/g" "$file" > "$tmp_file"

  echo "Creating parking facility from $file"
  response="$(post_json "$FACILITIES_BASE_URL/api/v1/parking-facilities" "$tmp_file")"
  id="$(printf '%s' "$response" | extract_json_id)"
  echo "Parking facility created: ${id:-unknown id}"
done

echo "Data initialization completed"
