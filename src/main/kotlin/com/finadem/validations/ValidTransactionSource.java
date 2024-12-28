package com.finadem.validations;

import com.finadem.enums.TransactionSource;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TransactionSourceValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransactionSource {

    String message() default "Invalid transaction source.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class TransactionSourceValidator implements ConstraintValidator<ValidTransactionSource, TransactionSource> {

    @Override
    public void initialize(ValidTransactionSource constraintAnnotation) {
    }

    @Override
    public boolean isValid(TransactionSource transactionSource, ConstraintValidatorContext context) {
        return transactionSource != null && TransactionSource.isValid(transactionSource);
    }
}