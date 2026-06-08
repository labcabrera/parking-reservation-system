package org.labcabrera.parking.facilities.application.service;

import org.axonframework.eventhandling.EventHandler;
import org.labcabrera.parking.facilities.domain.event.ReservationPaymentExpiredEvent;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationPaymentExpirationInventoryReleaseHandler {

    private final InventoryHoldService inventoryHoldService;

    @EventHandler
    public void on(ReservationPaymentExpiredEvent event) {
        log.info("Reservation {} payment expired; releasing inventory for order {}",
            event.reservationId(), event.orderId());
        inventoryHoldService.release(event.facilityId(), event.checkIn(), event.checkOut());
    }
}
