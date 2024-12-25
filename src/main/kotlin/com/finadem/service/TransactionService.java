package com.finadem.service;

import com.finadem.dto.AccountDTO;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface TransactionService {
    String createNewTransaction(String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
                                BigDecimal amount, TransactionType transactionType, String transactionRemarks);
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
        String isTransactionSuccess;
        BigDecimal currentBalance;
        Transaction transactionEntity = new Transaction();
        try {
            AccountDTO accountData = accountService.getAccountInformation(customerAccountNumber);
            if (transactionType == TransactionType.DEPOSIT) {
                boolean accountStatus = false;
                if (accountData == null) {
                    accountData = new AccountDTO();
                    accountData.setCurrency(currencyType);
                    accountData.setCurrentBalance(amount);
                    accountData.setAccountHolderName(customerAccountNumber);
                    accountData.setStatus(AccountStatus.ACTIVE_KYC_NOT_COMPLETED);
                    accountStatus = accountService.createNewAccount(accountData);
                }
                if (accountStatus) {
                    setTransactionData(transactionEntity,
                            transactingAccountNumber,
                            customerAccountNumber,
                            currencyType, amount, transactionType, transactionRemarks
                    );
                    currentBalance = accountData.getCurrentBalance() != null ? accountData.getCurrentBalance() : BigDecimal.ZERO;
                    accountService.updateAccountBalance(customerAccountNumber, currentBalance.add(amount));
                    isTransactionSuccess = "Deposit Successful";
                } else {
                    isTransactionSuccess = "Account creation failed or Transaction Failed";
                }

            } else {
                if (accountData == null) {
                    isTransactionSuccess = "Account does not exist";
                } else {
                    currentBalance = accountData.getCurrentBalance();
                    if (currentBalance.compareTo(BigDecimal.ZERO) > 0 && currentBalance.compareTo(amount) >= 0) {
                        setTransactionData(transactionEntity,
                                transactingAccountNumber,
                                customerAccountNumber,
                                currencyType, amount, transactionType, transactionRemarks
                        );
                        transactionRepository.save(transactionEntity);
                        accountService.updateAccountBalance(customerAccountNumber, currentBalance.subtract(amount));
                        isTransactionSuccess = "Withdrawal Successful";
                    } else {
                        isTransactionSuccess = "Insufficient Funds";
                    }
                }
            }
        } catch (
                Exception e) {
            logger.error("Error while creating transaction for account number {}", customerAccountNumber, e);
            isTransactionSuccess = "Transaction Failed";
        }
        return isTransactionSuccess;
    }


    private void setTransactionData(
            Transaction transactionEntity,
            String transactingAccountNumber, String customerAccountNumber, CurrencyEnum currencyType,
            BigDecimal amount, TransactionType transactionType, String transactionRemarks
    ) {
        transactionEntity.setTransactingAccount(transactingAccountNumber);
        transactionEntity.setCustomerAccount(customerAccountNumber);
        transactionEntity.setCurrency(currencyType);
        transactionEntity.setAmount(amount);
        transactionEntity.setType(transactionType);
        transactionEntity.setStatus(TransactionStatus.SUCCESS);
        transactionEntity.setTransactionRemarks(transactionRemarks);
    }
}
