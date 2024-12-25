package com.finadem.controller;

import com.finadem.dto.TransactionDTO;
import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionType;
import com.finadem.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/${api.version}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/newTransaction")
    public ResponseEntity<String> deposit(@RequestBody TransactionDTO transactionRequest) {
        String transactingAccountNumber = transactionRequest.getTransactingAccountNumber();
        String customerAccountNumber = transactionRequest.getCustomerAccountNumber();
        BigDecimal amount = transactionRequest.getAmount();
        CurrencyEnum currencyType = transactionRequest.getCurrencyType();
        TransactionType transactionType = transactionRequest.getTransactionType();
        String transactionRemarks = transactionRequest.getTransactionRemarks();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transaction amount must be greater than zero.");
        }
        transactionService.createNewTransaction(transactingAccountNumber, customerAccountNumber, currencyType, amount, transactionType, transactionRemarks);
        return ResponseEntity.status(HttpStatus.OK).body("Deposit successful.");
    }
}
