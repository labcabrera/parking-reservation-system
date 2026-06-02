package org.labcabrera.parking.catalog.domain.valueobject;

import java.time.LocalDateTime;
import java.util.Optional;

public record EntityMetadata(
    LocalDateTime createdAt,
    Optional<LocalDateTime> updatedAt,
    String ower) {
}
