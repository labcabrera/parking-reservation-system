package org.labcabrera.parking.facilities.application.cqrs.handler;

import java.time.Instant;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.labcabrera.parking.facilities.application.cqrs.command.CreateParkingFacilityCommand;
import org.labcabrera.parking.facilities.application.cqrs.command.DeleteParkingFacilityCommand;
import org.labcabrera.parking.facilities.application.cqrs.command.UpdateParkingFacilityCommand;
import org.labcabrera.parking.facilities.application.port.PricingServicePort;
import org.labcabrera.parking.facilities.domain.aggregate.ParkingFacility;
import org.labcabrera.parking.facilities.domain.event.ParkingFacilityCreatedEvent;
import org.labcabrera.parking.facilities.domain.exception.ConflictException;
import org.labcabrera.parking.facilities.domain.exception.EntityNotFoundException;
import org.labcabrera.parking.facilities.domain.port.ParkingFacilityRepository;
import org.labcabrera.parking.facilities.domain.valueobject.ParkingPricingRule;
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
    private final PricingServicePort pricingServicePort;

    @Transactional
    @CommandHandler
    public ParkingFacility handle(@Valid CreateParkingFacilityCommand command) {
        log.debug("Handling CreateParkingFacilityCommand: {}", command);
        if (repository.existsByName(command.name())) {
            throw new ConflictException("A parking facility with the same name already exists");
        }
        validatePricingRule(command.pricingRule());
        ParkingFacility parkingFacility = new ParkingFacility(
            command.name(),
            command.city(),
            command.address(),
            command.location(),
            command.capacity(),
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
            parkingFacility.getCapacity(),
            Instant.now()));
        return parkingFacility;
    }

    @Transactional
    @CommandHandler
    public ParkingFacility handle(@Valid UpdateParkingFacilityCommand command) {
        log.debug("Handling UpdateParkingFacilityCommand: {}", command);
        validatePricingRule(command.pricingRule());
        ParkingFacility parkingFacility = repository.findById(command.facilityId())
            .orElseThrow(() -> new EntityNotFoundException("Parking facility %s not found".formatted(command.facilityId())));
        parkingFacility.update(
            command.name(),
            command.city(),
            command.address(),
            command.location(),
            command.capacity(),
            command.tags(),
            command.status(),
            command.cancellationPolicy(),
            command.pricingRule());
        repository.save(parkingFacility);
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

    private void validatePricingRule(ParkingPricingRule pricingRule) {
        if (!pricingServicePort.existsById(pricingRule.externalPricingId())) {
            throw new EntityNotFoundException("Pricing rule %s not found".formatted(pricingRule.externalPricingId()));
        }
    }

}
