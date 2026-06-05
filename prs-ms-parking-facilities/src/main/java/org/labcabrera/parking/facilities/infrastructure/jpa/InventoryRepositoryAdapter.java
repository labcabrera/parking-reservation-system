package org.labcabrera.parking.facilities.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.labcabrera.parking.facilities.domain.port.InventoryRepository;
import org.labcabrera.parking.facilities.domain.valueobject.InventoryBlockType;
import org.labcabrera.parking.facilities.domain.valueobject.InventorySlot;
import org.labcabrera.parking.facilities.domain.valueobject.SlotKey;
import org.labcabrera.parking.facilities.infrastructure.jpa.entities.InventorySlotJpaEntity;
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
            .findByFacilityIdAndSlotStartAndBlockType(slot.facilityId(), slot.slotStart(), slot.blockType())
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
    public List<InventorySlot> findSlots(UUID facilityId, LocalDateTime start, LocalDateTime end) {
        return findSlots(facilityId, start, end, InventoryBlockType.SHORT_TERM);
    }

    @Override
    public List<InventorySlot> findSlots(UUID facilityId, LocalDateTime start, LocalDateTime end, InventoryBlockType blockType) {
        return repository.findSlotsByFacilityAndRange(facilityId, start, end, blockType)
            .stream()
            .map(e -> new InventorySlot(e.getSlotStart(), e.getBlockType(), e.getCapacity(), e.getReserved()))
            .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(List<SlotKey> slots, UUID facilityId) {
        for (SlotKey slot : slots) {
            repository.findByFacilityIdAndSlotStartAndBlockType(facilityId, slot.slotStart(), slot.blockType())
                .ifPresent(e -> repository.release(e.getId()));
        }
    }

    private InventorySlotJpaEntity createSlot(SlotKey slot, int capacity) {
        try {
            InventorySlotJpaEntity fresh = new InventorySlotJpaEntity(
                null, slot.facilityId(), slot.slotStart(), slot.blockType(), capacity, 0, 0L);
            return repository.saveAndFlush(fresh);
        }
        catch (DataIntegrityViolationException e) {
            // Concurrent insert won; re-read.
            return repository.findByFacilityIdAndSlotStartAndBlockType(slot.facilityId(), slot.slotStart(), slot.blockType())
                .orElseThrow(() -> e);
        }
    }
}
