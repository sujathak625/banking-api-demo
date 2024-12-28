package com.finadem.controller;

import com.finadem.entity.Transaction;
import com.finadem.exception.exceptions.InvalidDateFormatException;
import com.finadem.model.DepositWithdrawalRequest;
import com.finadem.model.FundTransferRequest;
import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionType;
import com.finadem.service.TransactionService;
import com.finadem.utilities.DateUtilities;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.NoTransactionException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    private DateUtilities dateUtilities;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@Valid @RequestBody DepositWithdrawalRequest transactionRequest) {
        return ResponseEntity.status(HttpStatus.OK).body("Deposit Successful.");

    }

    @PostMapping("/withdrawal")
    public ResponseEntity<String> withdrawal(@RequestBody DepositWithdrawalRequest transactionRequest) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Withdrawal Not successful.");
    }

    @PostMapping("/fundTransfer")
    public ResponseEntity<String> deposit(@RequestBody FundTransferRequest transactionRequest) {
        String transactingAccountNumber = transactionRequest.getTransactingAccountNumber();
        String customerAccountNumber = transactionRequest.getCustomerAccountNumber();
        BigDecimal amount = transactionRequest.getAmount();
        CurrencyEnum currencyType = transactionRequest.getCurrencyType();
        TransactionType transactionType = transactionRequest.getTransactionType();
        String transactionRemarks = transactionRequest.getTransactionRemarks();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transaction amount must be greater than zero.");
        }
        String transactionStatus = transactionService.createNewTransaction(transactingAccountNumber, customerAccountNumber, currencyType, amount, transactionType, transactionRemarks);
        return ResponseEntity.status(HttpStatus.OK).body("Deposit successful.");
    }

    @GetMapping("/history/{iban}/{n}")
    public ResponseEntity<List<Transaction>> getLastNTransactions(@PathVariable String iban, @PathVariable int n) {
        List<Transaction> transactionHistory = transactionService.getLastNTransactionHistory(iban, n);
        if (transactionHistory != null && transactionHistory.isEmpty()) {
            throw new NoTransactionException("No transactions found for IBAN: " + iban);
        }
        return ResponseEntity.status(HttpStatus.OK).body(transactionHistory);
    }

    @GetMapping("/history/{iban}/{fromDate}/{toDate}")
    public ResponseEntity<List<Transaction>> getTransactionHistoryBetween(
            @PathVariable String iban,
            @PathVariable String fromDate,
            @PathVariable String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDate = dateUtilities.validateAndParseDate(fromDate, formatter);
        LocalDate endDate = dateUtilities.validateAndParseDate(toDate, formatter);
        dateUtilities.isStartDateAfterEndDate(startDate,endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<Transaction> transactionsHistory = transactionService.getTransactionHistoryBetween(iban, startDateTime, endDateTime);
        if (transactionsHistory == null || transactionsHistory.isEmpty()) {
            throw new NoTransactionException("No transactions found between dates: " + fromDate + " and " + toDate);
        }
        return ResponseEntity.ok(transactionsHistory);
    }
}
