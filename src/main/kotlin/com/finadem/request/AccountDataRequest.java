package com.finadem.request;

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
public class AccountDataRequest {
    private Long customerId;
    private String accountNumber;
    private String accountHolderName;
    private String taxId;
    private CurrencyEnum currency;
    private BigDecimal currentBalance;
    private AccountStatus status;
}
