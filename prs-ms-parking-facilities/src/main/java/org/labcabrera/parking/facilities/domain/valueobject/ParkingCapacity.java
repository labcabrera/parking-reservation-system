package org.labcabrera.parking.facilities.domain.valueobject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "ParkingCapacity", description = "Parking capacity split by reservation duration")
public record ParkingCapacity(

    @Min(1) @Schema(description = "Total number of parking spots", examples = "120", minimum = "1") int total,

    @Min(0) @Schema(description = "Parking spots dedicated to short-duration reservations", examples = "80", minimum = "0") int shortTerm,

    @Min(0) @Schema(description = "Parking spots dedicated to long-duration reservations", examples = "40", minimum = "0") int longTerm) {

    public ParkingCapacity {
        if (total < 1) {
            throw new IllegalArgumentException("capacity.total must be at least 1");
        }
        if (shortTerm < 0) {
            throw new IllegalArgumentException("capacity.shortTerm must be zero or greater");
        }
        if (longTerm < 0) {
            throw new IllegalArgumentException("capacity.longTerm must be zero or greater");
        }
        if (shortTerm + longTerm != total) {
            throw new IllegalArgumentException("capacity.shortTerm + capacity.longTerm must equal capacity.total");
        }
    }

    public int capacityFor(InventoryBlockType blockType) {
        return blockType == InventoryBlockType.LONG_TERM ? longTerm : shortTerm;
    }
}
