package org.labcabrera.parking.catalog.domain.valueobjects;

import java.time.LocalDateTime;
import java.util.Optional;

public record EntityMetadata(
    LocalDateTime createdAt,
    Optional<LocalDateTime> updatedAt,
    String ower) {
}
