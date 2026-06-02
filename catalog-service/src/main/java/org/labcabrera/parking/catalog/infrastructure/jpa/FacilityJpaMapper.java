package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityId;
import org.labcabrera.parking.catalog.domain.valueobjects.FacilityTag;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FacilityJpaMapper {

    default ParkingFacility toDomain(ParkingFacilityJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        java.util.Set<FacilityTag> tags = entity.getTags() != null
            ? java.util.Arrays.stream(entity.getTags())
                .map(FacilityJpaMapper::safeTagValueOf)
                .filter(t -> t != null)
                .collect(java.util.stream.Collectors.toCollection(() -> java.util.EnumSet.noneOf(FacilityTag.class)))
            : java.util.EnumSet.noneOf(FacilityTag.class);

        org.labcabrera.parking.catalog.domain.valueobjects.Coordinates coords = new org.labcabrera.parking.catalog.domain.valueobjects.Coordinates(
            entity.getLatitude(), entity.getLongitude());

        org.labcabrera.parking.catalog.domain.valueobjects.CancellationPolicy cancellationPolicy = new org.labcabrera.parking.catalog.domain.valueobjects.CancellationPolicy(
            entity.getFreeCancelHours(), entity.getPenaltyCancelMinutes());

        org.labcabrera.parking.catalog.domain.valueobjects.ParkingPricingRule pricingRule = new org.labcabrera.parking.catalog.domain.valueobjects.ParkingPricingRule(
            null, entity.getDailyRate());

        return new ParkingFacility(
            FacilityId.of(entity.getId()),
            entity.getName(),
            entity.getCity(),
            entity.getAddress(),
            coords,
            entity.getTotalSpots(),
            tags,
            org.labcabrera.parking.catalog.domain.valueobjects.FacilityStatus.valueOf(entity.getStatus()),
            cancellationPolicy,
            pricingRule,
            entity.getVersion());
    }

    default FacilityId map(UUID id) {
        return id == null ? null : FacilityId.of(id);
    }

    default Set<FacilityTag> map(String[] tags) {
        if (tags == null) {
            return java.util.EnumSet.noneOf(FacilityTag.class);
        }
        return java.util.Arrays.stream(tags)
            .map(FacilityJpaMapper::safeTagValueOf)
            .filter(t -> t != null)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(FacilityTag.class)));
    }

    static FacilityTag safeTagValueOf(String tag) {
        try {
            return FacilityTag.valueOf(tag);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "latitude", expression = "java(domain.getLocation() != null ? domain.getLocation().latitude() : 0.0)")
    @Mapping(target = "longitude", expression = "java(domain.getLocation() != null ? domain.getLocation().longitude() : 0.0)")
    @Mapping(target = "totalSpots", source = "totalSpots")
    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "status", expression = "java(domain.getStatus() != null ? domain.getStatus().name() : null)")
    @Mapping(target = "freeCancelHours", expression = "java(domain.getCancellationPolicy() != null ? domain.getCancellationPolicy().freeCancelHours() : 0)")
    @Mapping(target = "penaltyCancelMinutes", expression = "java(domain.getCancellationPolicy() != null ? domain.getCancellationPolicy().penaltyCancelMinutes() : 0)")
    @Mapping(target = "dailyRate", expression = "java(domain.getPricingRule() != null ? domain.getPricingRule().estimatedDailyPrice() : null)")
    @Mapping(target = "currency", expression = "java(\"EUR\")")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ParkingFacilityJpaEntity toEntity(ParkingFacility domain);

    default java.util.UUID map(FacilityId id) {
        return id == null ? null : id.value();
    }

    default String[] map(java.util.Set<FacilityTag> tags) {
        if (tags == null) {
            return null;
        }
        return tags.stream().map(Enum::name).toArray(String[]::new);
    }
}
