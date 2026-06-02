package org.labcabrera.parking.catalog.interfaces.rest.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDto(UUID id, LocalDateTime expiresAt) {
}
