package org.labcabrera.parking.facilities.domain.exception;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

    private final String code;
    private final int status;

    protected DomainException(String code, int status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

}