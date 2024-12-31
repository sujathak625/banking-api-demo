package com.finadem.exception.exceptions;

public class InvalidTransactionType extends RuntimeException {
    public InvalidTransactionType(String message) {
        super(message);
    }
}
