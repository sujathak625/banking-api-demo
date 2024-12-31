package com.finadem.validation.validationTypes;

import com.finadem.helper.AccountHelper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = IbanValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIban {
    String message() default "Invalid IBAN.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


class IbanValidator implements ConstraintValidator<ValidIban, String> {
    AccountHelper accountHelper;

    public IbanValidator(AccountHelper accountHelper) {
        this.accountHelper = accountHelper;
    }

    @Override
    public void initialize(ValidIban constraintAnnotation) {
    }

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext context) {
        if (iban == null || iban.isBlank()) {
            return false;
        }
        return accountHelper.isIbanValid(iban);
    }
}
