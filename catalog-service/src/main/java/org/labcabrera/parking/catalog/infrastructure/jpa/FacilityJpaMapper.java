package org.labcabrera.parking.catalog.infrastructure.jpa;

import org.labcabrera.parking.catalog.domain.model.CancellationPolicy;
import org.labcabrera.parking.catalog.domain.model.Coordinates;
import org.labcabrera.parking.catalog.domain.model.FacilityId;
import org.labcabrera.parking.catalog.domain.model.FacilityStatus;
import org.labcabrera.parking.catalog.domain.model.FacilityTag;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FacilityJpaMapper {

    private FacilityJpaMapper() {}

    public static ParkingFacility toDomain(ParkingFacilityJpaEntity entity) {
        Set<FacilityTag> tags = entity.getTags() != null
                ? Arrays.stream(entity.getTags())
                        .map(FacilityTag::valueOf)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(FacilityTag.class)))
                : EnumSet.noneOf(FacilityTag.class);

        return new ParkingFacility(
                FacilityId.of(entity.getId()),
                entity.getName(),
                entity.getCity(),
                entity.getAddress(),
                new Coordinates(entity.getLatitude(), entity.getLongitude()),
                entity.getTotalSpots(),
                tags,
                FacilityStatus.valueOf(entity.getStatus()),
                new CancellationPolicy(entity.getFreeCancelHours(), entity.getPenaltyCancelMinutes()),
                entity.getVersion());
    }
}
