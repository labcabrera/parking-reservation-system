package org.labcabrera.parking.catalog.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.catalog.domain.valueobject.InventoryBlockType;

class SlotCalculatorTest {

    private static final UUID FACILITY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Duration THRESHOLD = Duration.ofHours(12);

    @Test
    void usesShortTermSlotsAtThreshold() {
        var plan = SlotCalculator.planFor(
            FACILITY_ID,
            LocalDateTime.of(2026, 6, 1, 8, 10),
            LocalDateTime.of(2026, 6, 1, 20, 10),
            THRESHOLD);

        assertEquals(InventoryBlockType.SHORT_TERM, plan.blockType());
        assertEquals(25, plan.slots().size());
        assertEquals(LocalDateTime.of(2026, 6, 1, 8, 0), plan.slots().get(0).slotStart());
        assertEquals(InventoryBlockType.SHORT_TERM, plan.slots().get(0).blockType());
    }

    @Test
    void usesWholeDaysAboveThreshold() {
        var plan = SlotCalculator.planFor(
            FACILITY_ID,
            LocalDateTime.of(2026, 6, 1, 8, 0),
            LocalDateTime.of(2026, 6, 2, 9, 0),
            THRESHOLD);

        assertEquals(InventoryBlockType.LONG_TERM, plan.blockType());
        assertEquals(2, plan.slots().size());
        assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0), plan.slots().get(0).slotStart());
        assertEquals(LocalDateTime.of(2026, 6, 2, 0, 0), plan.slots().get(1).slotStart());
        assertEquals(InventoryBlockType.LONG_TERM, plan.slots().get(0).blockType());
    }
}
