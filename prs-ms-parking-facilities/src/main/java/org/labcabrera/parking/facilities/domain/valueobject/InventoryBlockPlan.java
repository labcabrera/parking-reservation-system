package org.labcabrera.parking.facilities.domain.valueobject;

import java.util.List;

public record InventoryBlockPlan(InventoryBlockType blockType, List<SlotKey> slots) {
}
