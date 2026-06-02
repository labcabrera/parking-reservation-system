package org.labcabrera.parking.catalog.domain.aggregate;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

@Getter
public class ParkingInventorySlot {

    private UUID id;
    private String parkingFacilityId;
    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private int totalCapacity;
    private int reservedCount;
    private long version;
}
