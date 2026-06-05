package org.labcabrera.parking.facilities.domain.exception;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super("NOT_FOUND", 404, message);
    }

}
