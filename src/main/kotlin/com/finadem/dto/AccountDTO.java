package com.finadem.dto;

import com.finadem.enums.AccountStatus;
import com.finadem.enums.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Long customerId;
    private String accountNumber;
    private String accountHolderName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String taxId;
    private CurrencyEnum currency;
    private BigDecimal currentBalance;
    private AccountStatus status;
}
