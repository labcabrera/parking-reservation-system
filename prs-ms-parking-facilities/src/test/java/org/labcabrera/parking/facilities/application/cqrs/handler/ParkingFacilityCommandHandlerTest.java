package org.labcabrera.parking.facilities.application.cqrs.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Test;
import org.labcabrera.parking.facilities.application.cqrs.command.CreateParkingFacilityCommand;
import org.labcabrera.parking.facilities.application.port.PricingServicePort;
import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.facilities.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.facilities.domain.port.ParkingFacilityRepository;
import org.labcabrera.parking.facilities.domain.valueobject.CancellationPolicy;
import org.labcabrera.parking.facilities.domain.valueobject.Coordinates;
import org.labcabrera.parking.facilities.domain.valueobject.FacilityStatus;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingCapacity;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingPricingRule;

class ParkingFacilityCommandHandlerTest {

    private final ParkingFacilityRepository repository = mock(ParkingFacilityRepository.class);
    private final EventGateway eventGateway = mock(EventGateway.class);
    private final PricingServicePort pricingServicePort = mock(PricingServicePort.class);
    private final ParkingFacilityCommandHandler handler = new ParkingFacilityCommandHandler(
        repository,
        eventGateway,
        pricingServicePort);

    @Test
    void createsFacilityWhenPricingRuleExists() {
        UUID pricingRuleId = UUID.randomUUID();
        when(pricingServicePort.existsById(pricingRuleId)).thenReturn(true);

        ParkingFacility facility = handler.handle(command(pricingRuleId));

        assertThat(facility.getPricingRule().externalPricingId()).isEqualTo(pricingRuleId);
        verify(repository).save(facility);
    }

    @Test
    void rejectsFacilityWhenPricingRuleDoesNotExist() {
        UUID pricingRuleId = UUID.randomUUID();
        when(pricingServicePort.existsById(pricingRuleId)).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(command(pricingRuleId)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Pricing rule %s not found".formatted(pricingRuleId));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private CreateParkingFacilityCommand command(UUID pricingRuleId) {
        return new CreateParkingFacilityCommand(
            "Airport Parking",
            "Madrid",
            "Terminal 1",
            new Coordinates(40.1, -3.6),
            new ParkingCapacity(10, 6, 4),
            null,
            FacilityStatus.ACTIVE,
            new CancellationPolicy(24, 30),
            new ParkingPricingRule(pricingRuleId, new BigDecimal("30.00")));
    }
}
