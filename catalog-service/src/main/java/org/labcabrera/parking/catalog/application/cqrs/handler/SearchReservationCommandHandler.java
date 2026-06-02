package org.labcabrera.parking.catalog.application.cqrs.handler;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.labcabrera.parking.catalog.application.cqrs.command.StartReservationCommand;
import org.labcabrera.parking.catalog.domain.aggregate.Reservation;
import org.labcabrera.parking.catalog.domain.port.outbound.SearchReservationRepository;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class SearchReservationCommandHandler {

    //TODO config
    private static int expirationInSeconds = 600;

    private final SearchReservationRepository repository;
    private final EventGateway eventGateway;

    @Transactional
    @CommandHandler
    public Reservation handle(StartReservationCommand command) {
        log.debug("Handling StartReservationCommand: {}", command);
        //TODO integrar con seguridad
        String userId = "user-test";
        Reservation searchReservation = Reservation.create(userId, userId, userId, userId, expirationInSeconds);
        repository.save(searchReservation);
        return searchReservation;
    }

}
