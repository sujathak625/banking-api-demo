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

   // Validation Exceptions
    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<String> handleInvalidDateFormatException(InvalidDateFormatException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(StartDateAfterEndDateException.class)
    public ResponseEntity<String> handleStartDateAfterEndDateException(StartDateAfterEndDateException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleAccountBalanceLessThanZeroException(InsufficientBalanceException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidIbanException.class)
    public ResponseEntity<String> handleInvalidIbanException(InvalidIbanException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTransactionType.class)
    public ResponseEntity<String> handleInvalidTransactionType(InvalidTransactionType type){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(type.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

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

    @ExceptionHandler(TransferToSelfException.class)
    public ResponseEntity<String> handleTransferToSelfException(TransferToSelfException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(TransactionForbiddenException.class)
    public ResponseEntity<String> handleTransactionForbiddenException(TransactionForbiddenException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Transaction Execution Exceptions
    @ExceptionHandler(IbanNotFoundException.class)
    public ResponseEntity<String> handleIbanNotFoundException(IbanNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NoTransactionException.class)
    public ResponseEntity<String> handleNoTransactionsException(NoTransactionException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AccountCreationFailedException.class)
    public ResponseEntity<String> handleAccountCreationFailedException(AccountCreationFailedException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AccountDataUpdateFailedException.class)
    public ResponseEntity<String> handleAccountDataUpdateFailedException(AccountDataUpdateFailedException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
