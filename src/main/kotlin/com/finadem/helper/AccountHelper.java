package com.finadem.helper;

import com.finadem.configurations.IbanConfig;
import org.iban4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AccountHelper {

    Logger logger = LoggerFactory.getLogger(AccountHelper.class);
    IbanConfig ibanConfig;

    public AccountHelper(IbanConfig ibanConfig) {
        this.ibanConfig = ibanConfig;
    }

    public Long generateCustomerId(int numberOfDigits) {
        if (numberOfDigits < 1) {
            throw new IllegalArgumentException("Number of digits must be at least 1");
        }
        Random random = new Random();
        long lowerBound = (long) Math.pow(10, numberOfDigits - 1);
        long upperBound = (long) Math.pow(10, numberOfDigits) - 1;
        return lowerBound + random.nextInt((int) (upperBound - lowerBound + 1));
    }

    public String generateIBAN() {
        CountryCode countryCode = ibanConfig.getCountryCode();
        String bankCode = ibanConfig.getBankCode();
        Iban iban = new Iban.Builder()
                .countryCode(countryCode)
                .bankCode(bankCode)
                .buildRandom();
        return iban.toString();
    }

    public boolean isIbanValid(String iban) {
        boolean isValid;
        try {
            IbanUtil.validate(iban);
            isValid = true;
        } catch (IbanFormatException |
                 InvalidCheckDigitException |
                 UnsupportedCountryException e) {
            isValid = false;
            logger.error("Invalid IBAN {}", iban, e);
        }
        return isValid;
    }
}
