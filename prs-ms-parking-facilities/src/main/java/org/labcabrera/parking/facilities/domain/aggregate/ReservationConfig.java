package org.labcabrera.parking.facilities.domain.aggregate;

/**
 * Lightweight configuration value object injected into aggregate command handlers (Axon
 * resolves it from Spring context when the aggregate doesn't event-source it).
 */
public record ReservationConfig(int holdMinutes) {
}
