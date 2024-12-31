package com.finadem.service;

import com.finadem.enums.*;
import com.finadem.exception.exceptions.InsufficientBalanceException;
import com.finadem.exception.exceptions.IbanNotFoundException;
import com.finadem.exception.exceptions.InvalidTransactionType;
import com.finadem.exception.exceptions.TransferToSelfException;
import com.finadem.request.AccountDataRequest;
import com.finadem.entity.Transaction;
import com.finadem.repository.TransactionRepository;
import com.finadem.request.DepositWithdrawalRequest;
import com.finadem.request.FundTransferRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NoTransactionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    void createFundTransferTransaction(FundTransferRequest fundTransferRequest);

    void createDepositTransaction(String customerIban, CurrencyEnum currencyType,
                                  BigDecimal amount, String transactionRemarks,
                                  TransactionType transactionType,
                                  TransactionSource transactionSource);

    void createWithdrawalTransaction(DepositWithdrawalRequest withdrawalRequest);

    List<Transaction> getLastNTransactionHistory(String iban, int lastNTransactions);

    List<Transaction> getTransactionHistoryBetween(String iban, LocalDateTime startDate, LocalDateTime endDate);
}

@Service
class TransactionServiceImpl implements TransactionService {
    Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final AccountService accountService;
    private final CurrencyConverterService currencyConverterService;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountService accountService,
                                  CurrencyConverterService currencyConverterService,
                                  TransactionRepository transactionRepository
    ) {
        this.accountService = accountService;
        this.currencyConverterService = currencyConverterService;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void createFundTransferTransaction(FundTransferRequest fundTransferRequest) {
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(fundTransferRequest.getCustomerAccountNumber());
        if (fundTransferRequest.getTransactingAccountNumber().equals(fundTransferRequest.getCustomerAccountNumber())) {
            throw new TransferToSelfException("Sending and receiving account numbers cannot be the same.");
        }
        if (fundTransferRequest.getTransactionType().equals(TransactionType.DEPOSIT)
                || fundTransferRequest.getTransactionType().equals(TransactionType.WITHDRAWAL)) {
            throw new InvalidTransactionType("Please specify whether the transaction type is " + TransactionType.CREDIT_TRANSFER + " or " + TransactionType.DEBIT_TRANSFER + ". Other types not recognized");
        }
        BigDecimal transferRequestAmount = new BigDecimal(fundTransferRequest.getAmount());
        if (!fundTransferRequest.getCurrencyType().equals(CurrencyEnum.EUR)) {
            BigDecimal currentRate = currencyConverterService.getExchangeRate(CurrencyEnum.EUR.toString(), fundTransferRequest.getCurrencyType().toString());
            transferRequestAmount = transferRequestAmount.multiply(currentRate);
        }
        if (TransactionType.CREDIT_TRANSFER.equals(fundTransferRequest.getTransactionType())) {
            Transaction transactionEntity = Transaction.builder()
                    .iban(fundTransferRequest.getCustomerAccountNumber())
                    .amount(transferRequestAmount)
                    .type(TransactionType.CREDIT_TRANSFER)
                    .source(TransactionSource.FUND_TRANSFER)
                    .status(TransactionStatus.SUCCESS)
                    .currency(CurrencyEnum.EUR)
                    .transactionRemarks(fundTransferRequest.getTransactionRemarks())
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(fundTransferRequest.getCustomerAccountNumber(), accountDataRequest.getCurrentBalance().add(transferRequestAmount));
        } else if (TransactionType.DEBIT_TRANSFER.equals(fundTransferRequest.getTransactionType())) {
            BigDecimal prospectiveAccountBalance = accountDataRequest.getCurrentBalance().subtract(transferRequestAmount);
            if (prospectiveAccountBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            Transaction transactionEntity = Transaction.builder()
                    .iban(fundTransferRequest.getCustomerAccountNumber())
                    .amount(transferRequestAmount)
                    .type(TransactionType.DEBIT_TRANSFER)
                    .source(TransactionSource.FUND_TRANSFER)
                    .currency(CurrencyEnum.EUR)
                    .transactionRemarks(fundTransferRequest.getTransactionRemarks())
                    .status(TransactionStatus.SUCCESS)
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(fundTransferRequest.getCustomerAccountNumber(), prospectiveAccountBalance);
        }
    }

    @Override
    @Transactional
    public void createDepositTransaction(String customerIban, CurrencyEnum currencyType, BigDecimal amount, String transactionRemarks,
                                         TransactionType transactionType, TransactionSource transactionSource) {
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(customerIban);
        if (accountDataRequest != null) {
            Transaction transactionEntity = Transaction.builder()
                    .iban(customerIban)
                    .amount(amount)
                    .type(TransactionType.DEPOSIT)
                    .source(transactionSource)
                    .status(TransactionStatus.SUCCESS)
                    .currency(currencyType).transactionRemarks(transactionRemarks)
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(customerIban, accountDataRequest.getCurrentBalance().add(amount));
        } else {
            throw new IbanNotFoundException("Iban does not exist");
        }
    }

    @Override
    @Transactional
    public void createWithdrawalTransaction(DepositWithdrawalRequest withdrawalRequest) {
        String customerIban = withdrawalRequest.getIban();
        BigDecimal withdrawalAmount = new BigDecimal(withdrawalRequest.getAmount());
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(customerIban);
        if (accountDataRequest != null) {
            BigDecimal prospectiveAccountBalance = accountDataRequest.getCurrentBalance().subtract(withdrawalAmount);
            if (prospectiveAccountBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            Transaction transactionEntity = Transaction.builder()
                    .iban(withdrawalRequest.getIban())
                    .amount(withdrawalAmount)
                    .type(withdrawalRequest.getTransactionType())
                    .source(withdrawalRequest.getTransactionSource())
                    .currency(withdrawalRequest.getCurrency())
                    .transactionRemarks(withdrawalRequest.getTransactionRemarks())
                    .status(TransactionStatus.SUCCESS)
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(customerIban, prospectiveAccountBalance);
        } else {
            throw new IbanNotFoundException("Account with IBAN " + customerIban + " not found. To open a new account please contact the banking team.");
        }
    }

    @Override
    public List<Transaction> getLastNTransactionHistory(String iban, int numberOfTransactions) {
        Pageable pageable = PageRequest.of(0, numberOfTransactions);
        List<Transaction> transactionHistory = transactionRepository.getTransactionByAccountNumber(iban, pageable).getContent();
        if (transactionHistory.isEmpty()) {
            throw new NoTransactionException("No transactions found for IBAN: " + iban);
        }
        return transactionHistory;
    }

    @Override
    public List<Transaction> getTransactionHistoryBetween(String iban, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactionsHistory = transactionRepository.getTransactionHistoryBetween(iban, startDate, endDate);
        if (transactionsHistory == null || transactionsHistory.isEmpty()) {
            throw new NoTransactionException("No transactions found between dates: " + startDate + " and " + endDate);
        }
        return transactionsHistory;
    }
}
