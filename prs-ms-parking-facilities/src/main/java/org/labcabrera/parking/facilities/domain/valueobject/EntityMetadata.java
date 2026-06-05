package org.labcabrera.parking.facilities.domain.valueobject;

import java.time.LocalDateTime;
import java.util.Optional;

public record EntityMetadata(
    LocalDateTime createdAt,
    Optional<LocalDateTime> updatedAt,
    String ower) {
}
