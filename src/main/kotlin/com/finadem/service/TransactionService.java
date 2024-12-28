package com.finadem.service;

import com.finadem.model.AccountData;
import com.finadem.entity.Transaction;
import com.finadem.enums.AccountStatus;
import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionStatus;
import com.finadem.enums.TransactionType;
import com.finadem.repository.TransactionRepository;
import com.finadem.utilities.AccountUtilities;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    String createNewTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
                                BigDecimal amount, TransactionType transactionType, String transactionRemarks);

    String createDepositTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
                                    BigDecimal amount, String transactionRemarks);

    List<Transaction> getLastNTransactionHistory(String iban,int lastNTransactions);

    List<Transaction> getTransactionHistoryBetween(String iban,LocalDateTime startDate, LocalDateTime endDate);
}

@Service
class TransactionServiceImpl implements TransactionService {
    Logger logger = LoggerFactory.getLogger(TransactionService.class);
    public AccountService accountService;
    public TransactionRepository transactionRepository;
    public AccountUtilities accountUtilities;

    public TransactionServiceImpl(AccountService accountService, TransactionRepository transactionRepository,
                                  AccountUtilities accountUtilities) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.accountUtilities = accountUtilities;
    }

    @Transactional
    public String createNewTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
                                       BigDecimal amount, TransactionType transactionType, String transactionRemarks) {
        String isTransactionSuccessOrFailedMessage;
        BigDecimal currentBalance;
        Transaction transactionEntity = new Transaction();
        try {
            if (transactionType == TransactionType.DEPOSIT) {
                // information of receiving account is verified. Whether receiver's account
                // is actual bank's customer's account
                AccountData accountData = accountService.getAccountInformationByAccountNumber(customerAccountNumber);
                String newAccountNumber = null;
                if (accountData == null) {
                    accountData = new AccountData();
                    // to avoid duplicate creation of new account for transfer coming from same unknown account number
                  //  accountData.setCustomerId(Long.parseLong(customerAccountNumber));
                    accountData.setCurrency(currencyType);
                    accountData.setCurrentBalance(amount);
                    if(accountUtilities.isIbanValid(customerAccountNumber)) {
                        accountData.setAccountNumber(customerAccountNumber);
                        accountData.setAccountHolderName(customerAccountNumber);
                    }
                    accountData.setStatus(AccountStatus.ACTIVE_KYC_NOT_COMPLETED);
                    newAccountNumber = accountService.createNewAccount(accountData);
                }
                if (newAccountNumber!=null) { // block executes only when transfer is done to an unknown account number
                    // new account is opened with that unknown account number
                    setTransactionData(transactionEntity,
                            transactingAccountNumber,
                            newAccountNumber,
                            currencyType, amount, transactionType, transactionRemarks
                    );
                } else {
                    setTransactionData(transactionEntity,
                            transactingAccountNumber,
                            customerAccountNumber,
                            currencyType, amount, transactionType, transactionRemarks
                    );
                }
                currentBalance = accountData.getCurrentBalance() != null ? accountData.getCurrentBalance() : BigDecimal.ZERO;
                transactionRepository.save(transactionEntity);
                accountService.updateAccountBalance(customerAccountNumber, currentBalance.add(amount));
                isTransactionSuccessOrFailedMessage = "Deposit Successful";
            } else {
                // Here sender account number should be the bank's customer account.
                AccountData accountData = accountService.getAccountInformationByAccountNumber(transactingAccountNumber);
                if (accountData == null) {
                    isTransactionSuccessOrFailedMessage = "Account does not exist";
                    return isTransactionSuccessOrFailedMessage;
                } else {
                    currentBalance = accountData.getCurrentBalance();
                    if (currentBalance.compareTo(BigDecimal.ZERO) > 0 && currentBalance.compareTo(amount) >= 0) {
                        setTransactionData(transactionEntity,
                                transactingAccountNumber,
                                customerAccountNumber,
                                currencyType, amount, transactionType, transactionRemarks
                        );
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
    public String createDepositTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType, BigDecimal amount, String transactionRemarks) {
        return "";
    }

    @Override
    public List<Transaction> getLastNTransactionHistory(String iban, int numberOfTransactions) {
        Pageable pageable = PageRequest.of(0, numberOfTransactions);
        return transactionRepository.getTransactionByAccountNumber(iban, pageable).getContent();
    }

    @Override
    public List<Transaction> getTransactionHistoryBetween(String iban,LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getTransactionHistoryBetween(iban,startDate,endDate);
    }


    private void setTransactionData(
            Transaction transactionEntity,
            String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
            BigDecimal amount, TransactionType transactionType, String transactionRemarks
    ) {
        transactionEntity.setTransactingAccount(transactingAccountNumber);
        transactionEntity.setIban(customerAccountNumber);
        transactionEntity.setCurrency(currencyType);
        transactionEntity.setAmount(amount);
        transactionEntity.setType(transactionType);
        transactionEntity.setStatus(TransactionStatus.SUCCESS);
        transactionEntity.setTransactionRemarks(transactionRemarks);
    }
}
