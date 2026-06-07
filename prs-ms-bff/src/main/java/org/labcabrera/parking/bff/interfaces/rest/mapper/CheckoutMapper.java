package org.labcabrera.parking.bff.interfaces.rest.mapper;

import org.labcabrera.parking.bff.generated.client.ecommerce.model.Order;
import org.labcabrera.parking.bff.generated.client.facilities.model.FacilityAvailability;
import org.labcabrera.parking.bff.generated.client.facilities.model.Reservation;
import org.labcabrera.parking.bff.interfaces.rest.dto.CheckoutDto;
import org.labcabrera.parking.bff.interfaces.rest.dto.ParkingOptionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CheckoutMapper {

    @Mapping(source = "id", target = "facilityId")
    @Mapping(target = "lowAvailability", expression = "java(Boolean.TRUE.equals(facilityAvailability.getLowAvailability()))")
    ParkingOptionDto toParkingOptionDto(FacilityAvailability facilityAvailability);

    @Mapping(source = "reservation.id", target = "checkoutId")
    @Mapping(source = "reservation.status", target = "status")
    @Mapping(source = "reservation.facilityId", target = "facilityId")
    @Mapping(source = "reservation.checkIn", target = "checkIn")
    @Mapping(source = "reservation.checkOut", target = "checkOut")
    @Mapping(source = "reservation.expiresAt", target = "expiresAt")
    @Mapping(source = "reservation.estimatedPrice", target = "amount")
    @Mapping(source = "reservation.currency", target = "currency")
    @Mapping(target = "facilityName", ignore = true)
    @Mapping(target = "redirectUrl", ignore = true)
    @Mapping(target = "paymentStatus", expression = "java(order != null && order.getStatus() != null ? order.getStatus().getValue() : null)")
    CheckoutDto toCheckoutDtoInternal(Reservation reservation, Order order);

    default CheckoutDto toCheckoutDto(Reservation reservation, Order order) {
        if (reservation == null) {
            return null;
        }
        return toCheckoutDtoInternal(reservation, order);
    }
}
