package com.finadem.enums;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    CREDIT_TRANSFER,
    DEBIT_TRANSFER;

    public static boolean isValid(TransactionType transactionType) {
        for (TransactionType type : values()) {
            if (type == transactionType) {
                return true;
            }
        }
        return false;
    }
}
