package com.finadem.exception.exceptions;

public class StartDateAfterEndDateException extends RuntimeException {
    public StartDateAfterEndDateException(String message) {
        super(message);
    }
}
