package com.finadem.configurations;

import lombok.Getter;
import org.iban4j.CountryCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class IbanConfig {

    @Value("${iban.countryCode}")
    private CountryCode countryCode;

    @Value("${iban.bankCode}")
    private String bankCode;
}
