package com.finadem.service;

import com.finadem.enums.*;
import com.finadem.exception.exceptions.IbanNotFoundException;
import com.finadem.request.AccountDataRequest;
import com.finadem.entity.Transaction;
import com.finadem.repository.TransactionRepository;
import com.finadem.helper.AccountHelper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    String createNewTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
                                BigDecimal amount, TransactionType transactionType, String transactionRemarks);

    String createDepositTransaction(String customerIban, CurrencyEnum currencyType,
                                    BigDecimal amount, String transactionRemarks,
                                    TransactionType transactionType,
                                    TransactionSource transactionSource);

    boolean createWithdrawalTransaction(String customerIban, CurrencyEnum currencyType,BigDecimal amount,String transactionRemarks);

    List<Transaction> getLastNTransactionHistory(String iban, int lastNTransactions);

    List<Transaction> getTransactionHistoryBetween(String iban, LocalDateTime startDate, LocalDateTime endDate);
}

@Service
class TransactionServiceImpl implements TransactionService {
    Logger logger = LoggerFactory.getLogger(TransactionService.class);
    public AccountService accountService;
    public TransactionRepository transactionRepository;
    public AccountHelper accountHelper;

    public TransactionServiceImpl(AccountService accountService, TransactionRepository transactionRepository,
                                  AccountHelper accountHelper) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.accountHelper = accountHelper;
    }

    @Transactional
    public String createNewTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
                                       BigDecimal amount, TransactionType transactionType, String transactionRemarks) {
        String isTransactionSuccessOrFailedMessage;
        BigDecimal currentBalance;
        Transaction transactionEntity;
        try {
            if (transactionType == TransactionType.DEPOSIT) {
                // information of receiving account is verified. Whether receiver's account
                // is actual bank's customer's account
                AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(customerAccountNumber);
                String newAccountNumber = null;
                if (accountDataRequest == null) {
                    accountDataRequest = new AccountDataRequest();
                    // to avoid duplicate creation of new account for transfer coming from same unknown account number
                    //  accountData.setCustomerId(Long.parseLong(customerAccountNumber));
                    accountDataRequest.setCurrency(currencyType);
                    accountDataRequest.setCurrentBalance(amount);
                    if (accountHelper.isIbanValid(customerAccountNumber)) {
                        accountDataRequest.setAccountNumber(customerAccountNumber);
                        accountDataRequest.setAccountHolderName(customerAccountNumber);
                    }
                    accountDataRequest.setStatus(AccountStatus.ACTIVE_KYC_NOT_COMPLETED);
                    newAccountNumber = accountService.createNewAccount(accountDataRequest);
                }
                if (newAccountNumber != null) { // block executes only when transfer is done to an unknown account number
                    // new account is opened with that unknown account number
                    transactionEntity=
                            Transaction.builder().transactingAccount(customerAccountNumber)
                                    .iban(newAccountNumber)
                                    .currency(currencyType)
                                    .amount(amount)
                                    .type(transactionType)
                                    .transactionRemarks(transactionRemarks).build();
                } else {
                    transactionEntity=
                            Transaction.builder().transactingAccount(customerAccountNumber)
                                    .iban(customerAccountNumber)
                                    .currency(currencyType)
                                    .amount(amount)
                                    .type(transactionType)
                                    .transactionRemarks(transactionRemarks).build();
                }
                currentBalance = accountDataRequest.getCurrentBalance() != null ? accountDataRequest.getCurrentBalance() : BigDecimal.ZERO;
                transactionRepository.save(transactionEntity);
                accountService.updateAccountBalance(customerAccountNumber, currentBalance.add(amount));
                isTransactionSuccessOrFailedMessage = "Deposit Successful";
            } else {
                // Here sender account number should be the bank's customer account.
                AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(transactingAccountNumber);
                if (accountDataRequest == null) {
                    isTransactionSuccessOrFailedMessage = "Account does not exist";
                    return isTransactionSuccessOrFailedMessage;
                } else {
                    currentBalance = accountDataRequest.getCurrentBalance();
                    if (currentBalance.compareTo(BigDecimal.ZERO) > 0 && currentBalance.compareTo(amount) >= 0) {
                     transactionEntity=
                        Transaction.builder().transactingAccount(customerAccountNumber)
                                .iban(customerAccountNumber)
                                .currency(currencyType)
                                .amount(amount)
                                .type(transactionType)
                                .transactionRemarks(transactionRemarks).build();
                        transactionRepository.save(transactionEntity);
                        accountService.updateAccountBalance(transactingAccountNumber, currentBalance.subtract(amount));
                        isTransactionSuccessOrFailedMessage = "Withdrawal Successful";
                    } else {
                        isTransactionSuccessOrFailedMessage = "Insufficient Funds";
                    }
                }
            }
        } catch (
                Exception e) {
            logger.error("Error while creating transaction for account number {}", customerAccountNumber, e);
            isTransactionSuccessOrFailedMessage = "Transaction Failed";
        }
        return isTransactionSuccessOrFailedMessage;
    }

    @Override
    @Transactional
    public String createDepositTransaction(String customerIban, CurrencyEnum currencyType, BigDecimal amount, String transactionRemarks,
                                           TransactionType transactionType, TransactionSource transactionSource) {
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(customerIban);
        if (accountDataRequest != null) {
            Transaction transactionEntity = Transaction.builder()
                    .iban(customerIban)
                    .amount(amount)
                    .type(TransactionType.DEPOSIT)
                    .source(TransactionSource.ATM)
                    .status(TransactionStatus.SUCCESS)
                    .currency(currencyType).transactionRemarks(transactionRemarks)
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(customerIban, accountDataRequest.getCurrentBalance().add(amount));
            return "Deposit Successful";
        } else {
            throw new IbanNotFoundException("Iban does not exist");
        }
    }

    @Override
    @Transactional
    public boolean createWithdrawalTransaction(String customerIban, CurrencyEnum currencyType, BigDecimal amount, String transactionRemarks) {
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(customerIban);
        boolean isWithdrawalSuccessful;
        if (accountDataRequest != null) {
            Transaction transactionEntity = Transaction.builder()
                    .iban(customerIban)
                    .amount(amount)
                    .type(TransactionType.WITHDRAWAL)
                    .currency(currencyType).transactionRemarks(transactionRemarks)
                    .build();
            transactionRepository.save(transactionEntity);
            accountService.updateAccountBalance(customerIban, accountDataRequest.getCurrentBalance().subtract(amount));
            isWithdrawalSuccessful = true;
        } else {
            isWithdrawalSuccessful = false;
            throw new IbanNotFoundException("Account with IBAN " + customerIban + " not found. To open a new account please contact the banking team.");
        }
        return isWithdrawalSuccessful;
    }

    @Override
    public List<Transaction> getLastNTransactionHistory(String iban, int numberOfTransactions) {
        Pageable pageable = PageRequest.of(0, numberOfTransactions);
        return transactionRepository.getTransactionByAccountNumber(iban, pageable).getContent();
    }

    @Override
    public List<Transaction> getTransactionHistoryBetween(String iban, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getTransactionHistoryBetween(iban, startDate, endDate);
    }
}
