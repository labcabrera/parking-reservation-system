package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.port.InventoryRepository;
import org.labcabrera.parking.catalog.domain.valueobject.SlotKey;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventorySlotJpaRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryHold(SlotKey slot, int defaultCapacity) {
        InventorySlotJpaEntity entity = repository
            .findByFacilityIdAndSlotStart(slot.facilityId(), slot.slotStart())
            .orElseGet(() -> createSlot(slot, defaultCapacity));

        if (entity.getReserved() >= entity.getCapacity()) {
            return false;
        }
        int updated = repository.tryHold(entity.getId(), entity.getVersion());
        if (updated == 0) {
            log.debug("Optimistic lock collision on slot {} (facility={}, start={})",
                entity.getId(), slot.facilityId(), slot.slotStart());
        }
        return updated == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(List<SlotKey> slots, UUID facilityId) {
        for (SlotKey slot : slots) {
            repository.findByFacilityIdAndSlotStart(facilityId, slot.slotStart())
                .ifPresent(e -> repository.release(e.getId()));
        }
    }

    private InventorySlotJpaEntity createSlot(SlotKey slot, int capacity) {
        try {
            InventorySlotJpaEntity fresh = new InventorySlotJpaEntity(
                null, slot.facilityId(), slot.slotStart(), capacity, 0, 0L);
            return repository.saveAndFlush(fresh);
        }
        catch (DataIntegrityViolationException e) {
            // Concurrent insert won; re-read.
            return repository.findByFacilityIdAndSlotStart(slot.facilityId(), slot.slotStart())
                .orElseThrow(() -> e);
        }
    }
}
