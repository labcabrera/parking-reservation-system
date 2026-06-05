package org.labcabrera.parking.facilities.domain.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockPlan;
import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockType;
import org.labcabrera.parking.facilities.domain.valueobject.SlotKey;

public final class SlotCalculator {

    public static final int SLOT_MINUTES = 30;

    private SlotCalculator() {
    }

    /**
     * Returns the ordered list of half-hour slots covering [checkIn, checkOut). checkIn
     * is floored and checkOut is ceiled to the 30-minute grid.
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
            slots.add(new SlotKey(facilityId, start.plusMinutes((long) i * SLOT_MINUTES), InventoryBlockType.SHORT_TERM));
        }
        return slots;
    }

    public static InventoryBlockPlan planFor(
        UUID facilityId,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        Duration shortDurationThreshold) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        Duration duration = Duration.between(checkIn, checkOut);
        if (!duration.minus(shortDurationThreshold).isPositive()) {
            return new InventoryBlockPlan(InventoryBlockType.SHORT_TERM, slotsFor(facilityId, checkIn, checkOut));
        }
        return new InventoryBlockPlan(InventoryBlockType.LONG_TERM, dailySlotsFor(facilityId, checkIn, checkOut));
    }

    public static List<SlotKey> dailySlotsFor(UUID facilityId, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut must be after checkIn");
        }
        LocalDate start = checkIn.toLocalDate();
        LocalDate endExclusive = checkOut.toLocalTime().equals(LocalTime.MIDNIGHT)
            ? checkOut.toLocalDate()
            : checkOut.toLocalDate().plusDays(1);
        int count = (int) ChronoUnit.DAYS.between(start, endExclusive);
        List<SlotKey> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            slots.add(new SlotKey(facilityId, start.plusDays(i).atStartOfDay(), InventoryBlockType.LONG_TERM));
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
