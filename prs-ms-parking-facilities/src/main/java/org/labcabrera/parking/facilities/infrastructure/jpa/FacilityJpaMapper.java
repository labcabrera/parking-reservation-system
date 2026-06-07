package org.labcabrera.parking.facilities.infrastructure.jpa;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.facilities.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.facilities.domain.valueobject.Coordinates;
import org.labcabrera.parking.facilities.domain.valueobject.EntityMetadata;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityId;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityTag;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingPricingRule;
import org.labcabrera.parking.facilities.infrastructure.jpa.entities.ParkingFacilityJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacilityJpaMapper {

    default ParkingFacility toDomain(ParkingFacilityJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Set<FacilityTag> tags = entity.getTags() != null
            ? Arrays.stream(entity.getTags())
                .map(FacilityJpaMapper::safeTagValueOf)
                .filter(t -> t != null)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FacilityTag.class)))
            : EnumSet.noneOf(FacilityTag.class);
        Coordinates coords = new Coordinates(entity.getLatitude(), entity.getLongitude());
        CancellationPolicy cancellationPolicy = new CancellationPolicy(entity.getFreeCancelHours(), entity.getPenaltyCancelMinutes());
        ParkingPricingRule pricingRule = new ParkingPricingRule(UUID.fromString(entity.getExternalPricingId()), entity.getEstimatedDailyPrice());
        ParkingCapacity capacity = capacityFrom(entity);
        EntityMetadata metadata = new EntityMetadata(
            entity.getCreatedAt(),
            Optional.ofNullable(entity.getUpdatedAt()),
            "system");
        return new ParkingFacility(
            FacilityId.of(entity.getId()),
            entity.getName(),
            entity.getCity(),
            entity.getAddress(),
            coords,
            capacity,
            tags,
            entity.getStatus(),
            cancellationPolicy,
            pricingRule,
            metadata,
            entity.getVersion());
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "latitude", source = "location.latitude")
    @Mapping(target = "longitude", source = "location.longitude")
    @Mapping(target = "totalSpots", source = "capacity.total")
    @Mapping(target = "shortTermSpots", source = "capacity.shortTerm")
    @Mapping(target = "longTermSpots", source = "capacity.longTerm")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "freeCancelHours", source = "cancellationPolicy.freeCancelHours")
    @Mapping(target = "penaltyCancelMinutes", source = "cancellationPolicy.penaltyCancelMinutes")
    @Mapping(target = "externalPricingId", source = "pricingRule.externalPricingId")
    @Mapping(target = "estimatedDailyPrice", source = "pricingRule.estimatedDailyPrice")
    @Mapping(target = "createdAt", source = "metadata.createdAt")
    @Mapping(target = "updatedAt", expression = "java(domain.getMetadata() != null ? domain.getMetadata().updatedAt().orElse(null) : null)")
    ParkingFacilityJpaEntity toEntity(ParkingFacility domain);

    default UUID map(FacilityId id) {
        return id == null ? null : id.value();
    }

    default String[] map(Set<FacilityTag> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream().map(Enum::name).toArray(String[]::new);
    }

    default String map(UUID id) {
        return id == null ? null : id.toString();
    }

    static FacilityTag safeTagValueOf(String tag) {
        try {
            return FacilityTag.valueOf(tag);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static ParkingCapacity capacityFrom(ParkingFacilityJpaEntity entity) {
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
