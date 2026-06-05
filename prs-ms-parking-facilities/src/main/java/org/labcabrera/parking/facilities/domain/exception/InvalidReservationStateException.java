package org.labcabrera.parking.facilities.domain.exception;

public class InvalidReservationStateException extends DomainException {

    public InvalidReservationStateException(String message) {
        super("INVALID_RESERVATION_STATE", 400, message);
    }

}
