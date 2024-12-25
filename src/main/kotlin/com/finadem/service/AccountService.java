package com.finadem.service;

import com.finadem.dto.AccountDTO;
import com.finadem.entity.Account;
import com.finadem.repository.AccountRepository;
import com.finadem.utilities.AccountUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface AccountService {
    boolean createNewAccount(AccountDTO account);

    boolean updateAccountBalance(String accountNumber, BigDecimal accountBalance);

    AccountDTO getAccountInformation(String accountNumber);
}

@Service
class AccountServiceImpl implements AccountService {

    Logger logger = LoggerFactory.getLogger(AccountService.class);
    AccountRepository accountRepository;
    private final AccountUtilities accountUtilities;

    final String UNKNOWN = "Unknown-";

    public AccountServiceImpl(AccountRepository accountRepository, AccountUtilities accountUtilities) {
        this.accountRepository = accountRepository;
        this.accountUtilities = accountUtilities;
    }

    public boolean createNewAccount(AccountDTO account) {
        boolean accountCreationStatus = false;
        boolean isExists = accountRepository.findAccountInformationByAccountNumber(account.getAccountNumber()) != null;
        Account accountEntity = new Account();
        if (!isExists) {
            accountEntity.setAccountHolderName(UNKNOWN + account.getAccountNumber());
            accountEntity.setCustomerId(generateUniqueCustomerId());
            accountEntity.setAccountNumber(accountUtilities.generateIBAN());
            if(account.getTaxId() == null) {
                accountEntity.setTaxId(UNKNOWN);
            } else {
                accountEntity.setTaxId(account.getTaxId());
            }
            accountEntity.setCurrentBalance(account.getCurrentBalance());
            accountEntity.setCurrency(account.getCurrency());
            if (account.getStatus() != null) {
                accountEntity.setStatus(account.getStatus());
            }
        }
        try {
            accountRepository.save(accountEntity);
            accountCreationStatus = true;
        } catch (Exception e) {
            logger.error("Error while creating account for account number {}", account.getAccountNumber(), e);
        }
        return accountCreationStatus;
    }

    @Override
    public boolean updateAccountBalance(String accountNumber, BigDecimal accountBalance) {
        try {
            Account account = accountRepository.findAccountInformationByAccountNumber(accountNumber);
            if (account == null) {
                logger.error("Account with account number {} not found", accountNumber);
                return false;
            }
            account.setCurrentBalance(accountBalance);
            accountRepository.save(account);
            return true;
        } catch (Exception e) {
            logger.error("Error while updating account balance for account number {}", accountNumber, e);
            return false;
        }
    }


    public AccountDTO getAccountInformation(String accountNumber) {
        Account accountEntity = accountRepository.findAccountInformationByAccountNumber(accountNumber);
        AccountDTO accountData = new AccountDTO();
        if (accountEntity != null) {
            accountData.setCustomerId(accountEntity.getCustomerId());
            accountData.setAccountHolderName(accountEntity.getAccountHolderName());
            accountData.setAccountNumber(accountEntity.getAccountNumber());
            accountData.setCreatedAt(accountEntity.getCreatedAt());
            accountData.setCurrentBalance(accountEntity.getCurrentBalance());
            accountData.setCurrency(accountEntity.getCurrency());
            accountData.setTaxId(accountEntity.getTaxId());
            accountData.setUpdatedAt(accountEntity.getUpdatedAt());
            accountData.setStatus(accountEntity.getStatus());
        } else {
            return null;
        }
        return accountData;
    }

    private Long generateUniqueCustomerId() {
        long customerId;
        do {
            customerId = accountUtilities.generateCustomerId(9);
        } while (accountRepository.existsById(Math.toIntExact(customerId)));
        return customerId;
    }
}
