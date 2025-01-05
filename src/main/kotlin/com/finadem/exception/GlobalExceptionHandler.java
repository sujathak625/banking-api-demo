package com.finadem.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.finadem.exception.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.NoTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            InvalidDateFormatException.class,
            StartDateAfterEndDateException.class,
            InsufficientBalanceException.class,
            InvalidIbanException.class,
            InvalidTransactionType.class,
            TransferToSelfException.class,
            TransactionForbiddenException.class,
            AccountCreationFailedException.class,
            AccountDataUpdateFailedException.class
    })
    public ResponseEntity<String> handleBadRequestExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Validation Exceptions thrown during call of the API endpoints.
    // Request not sent unless validation errors are cleared
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // When Enum values are not properly given ie giving any other value other than actual accepted Enum
    // values, the validation error is thrown
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormatException(InvalidFormatException ex) {
        Map<String, String> errorDetails = new HashMap<>();
        if (ex.getTargetType() != null && ex.getTargetType().isEnum()) {
            String fieldName = ex.getPath().getLast().getFieldName();
            String invalidValue = ex.getValue().toString();
            String validValues = Arrays.toString(ex.getTargetType().getEnumConstants());
            String errorMessage = String.format(
                    "Invalid enum value: '%s' for the field: '%s'. The value must be one of: %s.",
                    invalidValue, fieldName, validValues
            );
            errorDetails.put(fieldName, errorMessage);
        } else {
            errorDetails.put("error", "Invalid value provided.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }

    // Transaction related exceptions such as no IBAN or
    // no transactions for the IBAN exist in the database
    @ExceptionHandler({
            IbanNotFoundException.class,
            NoTransactionException.class
    })
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

