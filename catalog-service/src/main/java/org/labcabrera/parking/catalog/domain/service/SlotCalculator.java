package org.labcabrera.parking.catalog.domain.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.valueobject.SlotKey;

public final class SlotCalculator {

    public static final int SLOT_MINUTES = 30;

    private SlotCalculator() {
    }

    /**
     * Returns the ordered list of half-hour slots covering [checkIn, checkOut).
     * checkIn is floored and checkOut is ceiled to the 30-minute grid.
     */
    public static List<SlotKey> slotsFor(UUID facilityId, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        LocalDateTime start = floorToSlot(checkIn);
        LocalDateTime end = ceilToSlot(checkOut);
        long minutes = Duration.between(start, end).toMinutes();
        int count = (int) (minutes / SLOT_MINUTES);
        List<SlotKey> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            slots.add(new SlotKey(facilityId, start.plusMinutes((long) i * SLOT_MINUTES)));
        }
        return slots;
    }

    public static LocalDateTime floorToSlot(LocalDateTime t) {
        int minute = t.getMinute();
        int floored = (minute / SLOT_MINUTES) * SLOT_MINUTES;
        return t.withMinute(floored).withSecond(0).withNano(0);
    }

    public static LocalDateTime ceilToSlot(LocalDateTime t) {
        LocalDateTime floor = floorToSlot(t);
        return floor.equals(t) ? floor : floor.plusMinutes(SLOT_MINUTES);
    }
}
