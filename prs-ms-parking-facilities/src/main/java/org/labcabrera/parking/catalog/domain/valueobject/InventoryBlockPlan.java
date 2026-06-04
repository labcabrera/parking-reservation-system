package org.labcabrera.parking.catalog.domain.valueobject;

import java.util.List;

public record InventoryBlockPlan(InventoryBlockType blockType, List<SlotKey> slots) {
}
