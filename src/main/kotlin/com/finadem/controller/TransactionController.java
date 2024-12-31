package com.finadem.controller;

import com.finadem.entity.Transaction;
import com.finadem.request.DepositWithdrawalRequest;
import com.finadem.request.FundTransferRequest;
import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionType;
import com.finadem.service.TransactionService;
import com.finadem.helper.DateHelper;
import jakarta.validation.Valid;
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

    private final DateHelper dateHelper;

    public TransactionController(TransactionService transactionService, DateHelper dateHelper) {
        this.transactionService = transactionService;
        this.dateHelper = dateHelper;
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> depositFund(@Valid @RequestBody DepositWithdrawalRequest depositRequest) {
        transactionService.createDepositTransaction(depositRequest.getIban(),
                depositRequest.getCurrency(),
                new BigDecimal(depositRequest.getAmount()),
                depositRequest.getTransactionRemarks(),
                depositRequest.getTransactionType(),
                depositRequest.getTransactionSource());
        return ResponseEntity.status(HttpStatus.OK).body("Deposit Successful.");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawal(@Valid @RequestBody DepositWithdrawalRequest withdrawalRequest) {
        transactionService.createWithdrawalTransaction(withdrawalRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Withdrawal successful.");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferFunds(@Valid @RequestBody FundTransferRequest fundTransferRequest) {
        transactionService.createFundTransferTransaction(fundTransferRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Fund Transfer successful.");
    }

    @GetMapping("/history/{iban}/{n}")
    public ResponseEntity<List<Transaction>> getLastNTransactions(@PathVariable String iban, @PathVariable int n) {
        List<Transaction> transactionHistory = transactionService.getLastNTransactionHistory(iban, n);
        return ResponseEntity.status(HttpStatus.OK).body(transactionHistory);
    }

    @GetMapping("/history/{iban}/{fromDate}/{toDate}")
    public ResponseEntity<List<Transaction>> getTransactionHistoryBetween(@PathVariable String iban, @PathVariable String fromDate, @PathVariable String toDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate startDate = dateHelper.validateAndParseDate(fromDate, formatter);
        LocalDate endDate = dateHelper.validateAndParseDate(toDate, formatter);
        dateHelper.isStartDateAfterEndDate(startDate, endDate);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        List<Transaction> transactionsHistory = transactionService.getTransactionHistoryBetween(iban, startDateTime, endDateTime);
        return ResponseEntity.status(HttpStatus.OK).body(transactionsHistory);
    }
}
