package org.labcabrera.parking.facilities.domain.exception;

public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super("CONFLICT", 409, message);
    }

}
