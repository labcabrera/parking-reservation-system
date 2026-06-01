#!/bin/bash
# init-topics.sh — Idempotent Kafka topic creation for Parking Reservation System
# Executed by the kafka-init container after the broker becomes healthy.
set -euo pipefail

KAFKA_BOOTSTRAP="kafka:9092"
REPLICATION_FACTOR=1

create_topic() {
  local topic="$1"
  local partitions="$2"
  echo "Creating topic: $topic (partitions=$partitions, replication-factor=$REPLICATION_FACTOR)"
  kafka-topics.sh \
    --bootstrap-server "$KAFKA_BOOTSTRAP" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$partitions" \
    --replication-factor "$REPLICATION_FACTOR"
}

# Reservation domain events
create_topic "parking.reservations" 3

# Payment request/result topics (SAGA communication)
create_topic "parking.payments.requests" 3
create_topic "parking.payments.results" 3

# Pricing request/result topics (Hold pricing saga)
create_topic "parking.pricing.requests" 3
create_topic "parking.pricing.results" 3

# Availability change events (SSE push)
create_topic "parking.availability.changes" 3

echo "All topics created successfully."
kafka-topics.sh --bootstrap-server "$KAFKA_BOOTSTRAP" --list
