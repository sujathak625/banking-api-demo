package com.finadem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CurrencyConverterService {
    @Value("${currency.api.key}")
    private String apiKey;

    @Value("${currency.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public CurrencyConverterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getExchangeRate(String baseCurrency, String toCurrency) {
        String finalUrl = String.format("%s?apikey=%s&base_currency=%s&currencies=%s", apiUrl, apiKey, baseCurrency, toCurrency);

        Map response = restTemplate.getForObject(finalUrl, Map.class);

        if (response == null || !response.containsKey("data")) {
            throw new IllegalArgumentException("Invalid response from currency API");
        }
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Object rateObject = data.get(toCurrency);

        if (rateObject instanceof Number) {
            return BigDecimal.valueOf(((Number) rateObject).doubleValue());
        } else {
            throw new IllegalArgumentException("Exchange rate is not a valid number");
        }
    }

}


