package com.finadem.configurations;

import org.iban4j.CountryCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IbanConfig {

    @Value("${iban.countryCode}")
    private CountryCode countryCode;

    @Value("${iban.bankCode}")
    private String bankCode;

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public String getBankCode() {
        return bankCode;
    }
}
