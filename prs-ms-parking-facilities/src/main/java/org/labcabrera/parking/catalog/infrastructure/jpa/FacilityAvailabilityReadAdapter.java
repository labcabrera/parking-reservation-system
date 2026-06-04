package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.labcabrera.parking.catalog.domain.port.FacilityAvailabilityReadModel;
import org.labcabrera.parking.catalog.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.catalog.domain.valueobject.InventoryBlockType;
import org.labcabrera.parking.catalog.domain.valueobject.ParkingCapacity;
import org.labcabrera.parking.catalog.infrastructure.jpa.entities.ParkingFacilityJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class FacilityAvailabilityReadAdapter implements FacilityAvailabilityReadModel {

    private final FacilityJpaRepository facilityRepository;
    private final InventorySlotJpaRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FacilityAvailabilityRow> findCandidates(String text, LocalDateTime gridStart, LocalDateTime gridEnd,
            int limit, InventoryBlockType blockType) {
        var candidates = facilityRepository
            .searchCandidates(text, FacilityStatus.ACTIVE, PageRequest.of(0, limit))
            .getContent();
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = candidates.stream().map(ParkingFacilityJpaEntity::getId).toList();
        Map<UUID, Integer> maxReservedByFacility = new HashMap<>();
        for (Object[] row : inventoryRepository.findMaxReservedByFacility(ids, gridStart, gridEnd, blockType)) {
            maxReservedByFacility.put((UUID) row[0], ((Number) row[1]).intValue());
        }
        return candidates.stream()
            .map(f -> new FacilityAvailabilityRow(
                f.getId(),
                f.getName(),
                f.getCity(),
                f.getAddress(),
                capacityFrom(f),
                maxReservedByFacility.getOrDefault(f.getId(), 0),
                f.getEstimatedDailyPrice()))
            .toList();
    }

    private ParkingCapacity capacityFrom(ParkingFacilityJpaEntity entity) {
        int total = entity.getTotalSpots();
        int shortTerm = entity.getShortTermSpots() != null ? entity.getShortTermSpots() : total;
        int longTerm = entity.getLongTermSpots() != null ? entity.getLongTermSpots() : Math.max(0, total - shortTerm);
        if (shortTerm + longTerm != total) {
            shortTerm = total;
            longTerm = 0;
        }
        return new ParkingCapacity(total, shortTerm, longTerm);
    }
}
