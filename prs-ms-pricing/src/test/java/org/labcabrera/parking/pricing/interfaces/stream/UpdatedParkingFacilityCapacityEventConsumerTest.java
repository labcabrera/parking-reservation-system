package org.labcabrera.parking.pricing.interfaces.stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.labcabrera.parking.pricing.application.event.UpdatedParkingFacilityCapacityEvent;
import org.labcabrera.parking.pricing.application.service.ParkingFacilityCapacityProjectionService;
import org.labcabrera.parking.pricing.domain.valueobject.PricingSlotType;

class UpdatedParkingFacilityCapacityEventConsumerTest {

    private final ParkingFacilityCapacityProjectionService service = mock(ParkingFacilityCapacityProjectionService.class);
    private final UpdatedParkingFacilityCapacityEventConsumer consumer = new UpdatedParkingFacilityCapacityEventConsumer();

    @Test
    void shouldDelegateEventToProjectionService() {
        var event = new UpdatedParkingFacilityCapacityEvent(
            UUID.randomUUID(),
            PricingSlotType.SHORT,
            List.of(new UpdatedParkingFacilityCapacityEvent.SlotCapacity(
                Duration.ofMinutes(30),
                new BigDecimal("0.75"))));

        consumer.updatedParkingFacilityCapacityConsumer(service).accept(event);

        verify(service).handle(event);
    }
}
