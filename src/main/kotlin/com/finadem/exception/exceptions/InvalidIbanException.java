package com.finadem.exception.exceptions;

public class InvalidIbanException extends RuntimeException {
    public InvalidIbanException(String message, String accountNumber) {
        super(message);
    }
}
