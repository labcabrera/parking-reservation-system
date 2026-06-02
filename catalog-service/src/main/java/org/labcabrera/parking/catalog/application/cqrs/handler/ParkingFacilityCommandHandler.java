package org.labcabrera.parking.catalog.application.cqrs.handler;

import java.time.Instant;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.CreateParkingFacilityCommand;
import org.labcabrera.parking.catalog.domain.event.ParkingFacilityCreatedEvent;
import org.labcabrera.parking.catalog.domain.model.ParkingFacility;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ParkingFacilityCommandHandler {

    private final ParkingFacilityRepository repository;
    private final EventGateway eventGateway;

    @Transactional
    @CommandHandler
    public ParkingFacility handle(CreateParkingFacilityCommand command) {
        ParkingFacility parkingFacility = new ParkingFacility(
            command.name(),
            command.city(),
            command.address(),
            command.location(),
            command.totalSpots(),
            command.tags(),
            command.status(),
            command.cancellationPolicy(),
            command.pricingRule());
        repository.save(parkingFacility);
        eventGateway.publish(new ParkingFacilityCreatedEvent(
            parkingFacility.getId().toString(),
            parkingFacility.getName(),
            parkingFacility.getCity(),
            parkingFacility.getAddress(),
            parkingFacility.getTotalSpots(),
            Instant.now()));
        return parkingFacility;
    }

}
