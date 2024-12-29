package com.finadem.exception.exceptions;

public class IbanNotFoundException extends RuntimeException {
    public IbanNotFoundException(String message) {
        super(message);
    }
}
