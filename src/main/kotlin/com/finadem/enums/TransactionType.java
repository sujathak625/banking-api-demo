package com.finadem.enums;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    CREDIT,
    DEBIT;

    public static boolean isValid(TransactionType transactionType) {
        for (TransactionType type : values()) {
            if (type == transactionType) {
                return true;
            }
        }
        return false;
    }
}
