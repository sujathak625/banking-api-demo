package com.finadem.service;

import com.finadem.enums.*;
import com.finadem.exception.exceptions.*;
import com.finadem.request.AccountDataRequest;
import com.finadem.entity.Transaction;
import com.finadem.repository.TransactionRepository;
import com.finadem.request.DepositWithdrawalRequest;
import com.finadem.request.FundTransferRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NoTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

public interface TransactionService {
    List<Transaction> getLastNTransactionHistory(String iban, int lastNTransactions);

    List<Transaction> getTransactionHistoryBetween(String iban, LocalDateTime startDate, LocalDateTime endDate);

    void createDepositTransaction(String customerIban, CurrencyEnum currencyType,
                                  BigDecimal amount, String transactionRemarks,
                                  TransactionType transactionType,
                                  TransactionSource transactionSource);

    void createWithdrawalTransaction(DepositWithdrawalRequest withdrawalRequest);

    void createFundTransferTransaction(FundTransferRequest fundTransferRequest);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDepositTransaction(String customerIban, CurrencyEnum currencyType, BigDecimal amount, String transactionRemarks,
                                         TransactionType transactionType, TransactionSource transactionSource) {
        BigDecimal transferRequestAmount = amount;
        if (!currencyType.equals(CurrencyEnum.EUR)) {
            BigDecimal currentRate = currencyConverterService.getExchangeRate(CurrencyEnum.EUR.toString(), currencyType.toString());
            transferRequestAmount = amount.multiply(currentRate);
        }
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(customerIban);
        if (accountDataRequest != null) {
            Transaction transactionEntity = Transaction.builder()
                    .iban(customerIban)
                    .transactingAccount(customerIban)
                    .amount(transferRequestAmount)
                    .type(TransactionType.DEPOSIT)
                    .source(transactionSource)
                    .status(TransactionStatus.SUCCESS)
                    .currency(CurrencyEnum.EUR).transactionRemarks(transactionRemarks)
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(customerIban, accountDataRequest.getCurrentBalance().add(amount));
        } else {
            accountDataRequest = AccountDataRequest.builder()
                    .iban(customerIban)
                    .accountHolderName(customerIban)
                    .currency(CurrencyEnum.EUR)
                    .status(AccountStatus.ACTIVE_KYC_NOT_COMPLETED)
                    .currentBalance(transferRequestAmount)
                    .build();
            accountService.createNewAccount(accountDataRequest);

            Transaction transactionEntity = Transaction.builder()
                    .iban(customerIban)
                    .transactingAccount(customerIban)
                    .amount(transferRequestAmount)
                    .type(TransactionType.DEPOSIT)
                    .source(transactionSource)
                    .status(TransactionStatus.SUCCESS)
                    .currency(CurrencyEnum.EUR).transactionRemarks(transactionRemarks)
                    .build();
            transactionRepository.save(transactionEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                    .transactingAccount(withdrawalRequest.getIban())
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
            logger.error("No account found for IBAN: " + withdrawalRequest.getIban());
            throw new IbanNotFoundException("Account with IBAN " + customerIban + " not found. To open a new account please contact the banking team.");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void createFundTransferTransaction(FundTransferRequest fundTransferRequest) {
        validateFundTransferRequest(fundTransferRequest);
        BigDecimal transferAmount = getTransferAmountInEUR(fundTransferRequest);
        boolean isCreditTransfer = TransactionType.CREDIT.equals(fundTransferRequest.getTransactionType());
        if (isCreditTransfer) {
            processCreditTransfer(fundTransferRequest, transferAmount);
        } else {
            processDebitTransfer(fundTransferRequest, transferAmount);
        }
    }

    private void validateFundTransferRequest(FundTransferRequest fundTransferRequest) {
        if (fundTransferRequest.getTransactingAccountNumber().equals(fundTransferRequest.getCustomerAccountNumber())) {
            throw new TransferToSelfException("Sending and receiving account numbers cannot be the same.");
        }
        if (!EnumSet.of(TransactionType.CREDIT, TransactionType.DEBIT).contains(fundTransferRequest.getTransactionType())) {
            throw new InvalidTransactionType("Please specify a valid transaction type: CREDIT or DEBIT. Other values not accepted");
        }
    }

    private BigDecimal getTransferAmountInEUR(FundTransferRequest fundTransferRequest) {
        BigDecimal transferAmount = new BigDecimal(fundTransferRequest.getAmount());
        if (!CurrencyEnum.EUR.equals(fundTransferRequest.getCurrencyType())) {
            BigDecimal exchangeRate = currencyConverterService.getExchangeRate(
                    CurrencyEnum.EUR.toString(),
                    fundTransferRequest.getCurrencyType().toString()
            );
            transferAmount = transferAmount.multiply(exchangeRate);
        }
        return transferAmount;
    }

    private void processCreditTransfer(FundTransferRequest fundTransferRequest, BigDecimal transferAmount) {
        AccountDataRequest senderAccount = accountService.getAccountInformationByAccountNumber(fundTransferRequest.getCustomerAccountNumber());
        saveTransaction(
                fundTransferRequest.getCustomerAccountNumber(),
                fundTransferRequest.getTransactingAccountNumber(),
                transferAmount,
                TransactionType.CREDIT,
                "Fund transfer from " + fundTransferRequest.getTransactingAccountNumber()
        );
        accountService.updateAccountBalance(fundTransferRequest.getCustomerAccountNumber(), senderAccount.getCurrentBalance().add(transferAmount));
        updateRecipientAccountBalance(fundTransferRequest, transferAmount.negate(), "to");
    }

    private void processDebitTransfer(FundTransferRequest fundTransferRequest, BigDecimal transferAmount) {
        AccountDataRequest senderAccount = accountService.getAccountInformationByAccountNumber(fundTransferRequest.getCustomerAccountNumber());
        if(senderAccount==null){
            throw new IbanNotFoundException("Account with IBAN " + fundTransferRequest.getCustomerAccountNumber() + " not found.");
        }
        BigDecimal newBalance = senderAccount.getCurrentBalance().subtract(transferAmount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
        saveTransaction(
                fundTransferRequest.getCustomerAccountNumber(),
                fundTransferRequest.getTransactingAccountNumber(),
                transferAmount,
                TransactionType.DEBIT,
                "Fund transfer to " + fundTransferRequest.getTransactingAccountNumber()
        );
        accountService.updateAccountBalance(fundTransferRequest.getCustomerAccountNumber(), newBalance);
        updateRecipientAccountBalance(fundTransferRequest, transferAmount, "from");
    }

    private void updateRecipientAccountBalance(FundTransferRequest fundTransferRequest, BigDecimal amount, String direction) {
        AccountDataRequest recipientAccount = accountService.getAccountInformationByAccountNumber(fundTransferRequest.getTransactingAccountNumber());
        if (recipientAccount != null) {
            BigDecimal updatedBalance = recipientAccount.getCurrentBalance().add(amount);
            accountService.updateAccountBalance(fundTransferRequest.getTransactingAccountNumber(), updatedBalance);

            saveTransaction(
                    fundTransferRequest.getTransactingAccountNumber(),
                    fundTransferRequest.getCustomerAccountNumber(),
                    amount.abs(),
                    amount.signum() > 0 ? TransactionType.CREDIT : TransactionType.DEBIT,
                    "Online fund transfer " + direction + " account " + fundTransferRequest.getCustomerAccountNumber()
            );
        }
    }

    private void saveTransaction(String iban, String transactingAccount, BigDecimal amount, TransactionType type, String remarks) {
        Transaction transaction = Transaction.builder()
                .iban(iban)
                .transactingAccount(transactingAccount)
                .amount(amount)
                .type(type)
                .source(TransactionSource.ONLINE_FUND_TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .currency(CurrencyEnum.EUR)
                .transactionRemarks(remarks)
                .build();
        transactionRepository.save(transaction);
    }
}
