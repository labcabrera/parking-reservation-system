package org.labcabrera.parking.catalog.application.cqrs.handler;

import java.time.Instant;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.CreateParkingFacilityCommand;
import org.labcabrera.parking.catalog.application.cqrs.command.DeleteParkingFacilityCommand;
import org.labcabrera.parking.catalog.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.catalog.domain.event.ParkingFacilityCreatedEvent;
import org.labcabrera.parking.catalog.domain.port.outbound.ParkingFacilityRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
@Validated
public class ParkingFacilityCommandHandler {

    private final ParkingFacilityRepository repository;
    private final EventGateway eventGateway;

    @Transactional
    @CommandHandler
    public ParkingFacility handle(@Valid CreateParkingFacilityCommand command) {
        log.debug("Handling CreateParkingFacilityCommand: {}", command);
        // Basic validations
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("name must be provided xx");
        }
        if (repository.existsByName(command.name())) {
            throw new IllegalArgumentException("A parking facility with the same name already exists");
        }
        if (command.pricingRule() == null || command.pricingRule().estimatedDailyPrice() == null
            || command.pricingRule().estimatedDailyPrice().doubleValue() <= 0.0) {
            throw new IllegalArgumentException("pricingRule.estimatedDailyPrice must be greater than 0");
        }
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
            parkingFacility.getId(),
            parkingFacility.getName(),
            parkingFacility.getCity(),
            parkingFacility.getAddress(),
            parkingFacility.getTotalSpots(),
            Instant.now()));
        return parkingFacility;
    }

    @Transactional
    @CommandHandler
    public void handle(DeleteParkingFacilityCommand command) {
        log.info("Handling DeleteParkingFacilityCommand: {}", command);
        // NOTE antes del borrado habria que comprobar que no tiene reservas activas y
        // que esta en un estado de pre-borrado por ejemplo
        // De cara a la demo hacemos el borrado logico directamente
        repository.findById(command.facilityId());
    }

}
