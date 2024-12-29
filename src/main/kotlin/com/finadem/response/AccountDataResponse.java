package com.finadem.response;

import com.finadem.enums.AccountStatus;
import com.finadem.enums.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDataResponse {
    private String accountNumber;
    private BigDecimal currentBalance;
    private CurrencyEnum currency;
}