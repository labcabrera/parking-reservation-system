package org.labcabrera.parking.catalog.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ParkingCapacityTest {

    @Test
    void exposesDedicatedCapacityByBlockType() {
        var capacity = new ParkingCapacity(120, 80, 40);

        assertEquals(120, capacity.total());
        assertEquals(80, capacity.capacityFor(InventoryBlockType.SHORT_TERM));
        assertEquals(40, capacity.capacityFor(InventoryBlockType.LONG_TERM));
    }

    @Test
    void requiresDedicatedCapacitiesToMatchTotal() {
        assertThrows(IllegalArgumentException.class, () -> new ParkingCapacity(120, 80, 50));
    }
}
