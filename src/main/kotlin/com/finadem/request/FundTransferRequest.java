package com.finadem.request;

import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionType;
import com.finadem.validation.validationTypes.AcceptedCurrency;
import com.finadem.validation.validationTypes.ValidIban;
import com.finadem.validation.validationTypes.ValidTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransferRequest {
    @NotNull(message = "Sender IBAN cannot be null.")
    @NotBlank(message = "Sender IBAN cannot be blank.")
    @ValidIban
    private String transactingAccountNumber;

    @NotBlank(message = "Sender BIC cannot be blank.")
    @NotNull(message = "Sender BIC cannot be null.")
    private String transactingAccountBIC;

    @ValidIban
    @NotNull(message = "Recipient IBAN cannot be null.")
    @NotBlank(message = "Recipient IBAN cannot be blank.")
    private String customerAccountNumber;

    @NotBlank(message = "Amount cannot be blank.")
    @Positive(message = "Amount must be a positive number.")
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private String amount;

    @NotNull(message = "Currency cannot be null.")
    @AcceptedCurrency
    private CurrencyEnum currencyType;

    @NotNull(message = "Transaction Type should not be null")
    @ValidTransactionType
    private TransactionType transactionType;

    private String transactionRemarks;
}
