package com.finadem.validation.validationTypes;

import com.finadem.enums.TransactionType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Constraint(validatedBy = TransactionTypeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransactionType {

    String message() default "Invalid transaction type.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class TransactionTypeValidator implements ConstraintValidator<ValidTransactionType, TransactionType> {

    @Override
    public void initialize(ValidTransactionType constraintAnnotation) {
    }

    @Override
    public boolean isValid(TransactionType transactionType, ConstraintValidatorContext context) {
        return transactionType != null && TransactionType.isValid(transactionType);
    }
}

