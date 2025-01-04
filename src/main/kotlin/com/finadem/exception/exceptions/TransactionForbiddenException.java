package com.finadem.exception.exceptions;

public class TransactionForbiddenException extends RuntimeException {
    public TransactionForbiddenException(String message) {
        super(message);
    }
}
