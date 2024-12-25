package com.finadem.dto;

import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private String transactingAccountNumber;
    private String customerAccountNumber;
    private BigDecimal amount;
    private CurrencyEnum currencyType;
    private TransactionType transactionType;
    private String transactionRemarks;
}
