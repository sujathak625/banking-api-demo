package com.finadem.enums;

public enum TransactionSource {
    BANK_COUNTER,
    ATM,
    FUND_TRANSFER;

    public static boolean isValid(TransactionSource source) {
        for (TransactionSource transactionSource : values()) {
            if (transactionSource == source) {
                return true;
            }
        }
        return false;
    }
}
