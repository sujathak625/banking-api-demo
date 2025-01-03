package com.finadem.request;

import com.finadem.enums.AccountStatus;
import com.finadem.enums.CurrencyEnum;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDataRequest {
    private Long customerId;
    private String iban;
    private String accountHolderName;
    private String taxId;
    private CurrencyEnum currency;
    private BigDecimal currentBalance;
    private AccountStatus status;
}
