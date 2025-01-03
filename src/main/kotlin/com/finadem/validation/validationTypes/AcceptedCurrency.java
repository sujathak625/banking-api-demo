package com.finadem.validation.validationTypes;

import com.finadem.enums.CurrencyEnum;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CurrencyEnumValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AcceptedCurrency {

    String message() default "Only EUR and USD currency are accepted";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class CurrencyEnumValidator implements ConstraintValidator<AcceptedCurrency, CurrencyEnum> {

    @Override
    public void initialize(AcceptedCurrency constraintAnnotation) {

    }

    @Override
    public boolean isValid(CurrencyEnum currencyEnum, ConstraintValidatorContext context) {
        return currencyEnum != null && CurrencyEnum.isValid(currencyEnum);
    }
}
