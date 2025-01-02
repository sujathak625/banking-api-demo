package com.finadem.enums;

public enum CurrencyEnum {
    USD,
    EUR,
    GBP;
    public static boolean isValid(CurrencyEnum currency) {
        for (CurrencyEnum curr : values()) {
            if (curr == currency) {
                return true;
            }
        }
        return false;
    }
}