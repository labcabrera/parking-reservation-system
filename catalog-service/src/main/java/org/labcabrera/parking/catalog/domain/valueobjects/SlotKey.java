package org.labcabrera.parking.catalog.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Identifies a half-hour inventory bucket for a facility.
 * slotStart is the inclusive lower bound; slotEnd = slotStart + 30 minutes.
 */
public record SlotKey(UUID facilityId, LocalDateTime slotStart) {
}
