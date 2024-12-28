package com.finadem.service;

import com.finadem.model.AccountData;
import com.finadem.entity.Account;
import com.finadem.repository.AccountRepository;
import com.finadem.utilities.AccountUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountService {
    String createNewAccount(AccountData account);

    boolean updateAccountBalance(String accountNumber, BigDecimal accountBalance);

    AccountData getAccountInformationByAccountNumber(String accountNumber);

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

    public String createNewAccount(AccountData account) {
        String newAccountNumber = null;
        boolean isExists = accountRepository.findAccountInformationByAccountNumber(account.getAccountNumber()) != null;
        Account accountEntity = new Account();
        if (!isExists) {
            if (account.getCustomerId() == null) {
                accountEntity.setCustomerId(generateUniqueCustomerId());
            } else {
                accountEntity.setCustomerId(account.getCustomerId());
            }
            if (account.getAccountNumber() == null) {
                accountEntity.setAccountNumber(accountUtilities.generateIBAN());
            } else {
                accountEntity.setAccountNumber(account.getAccountNumber());
            }
            accountEntity.setAccountHolderName(UNKNOWN + "-" + account.getAccountHolderName());
            if (account.getTaxId() == null) {
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
            newAccountNumber = accountEntity.getAccountNumber();
        } catch (Exception e) {
            logger.error("Error while creating account for account number {}", account.getAccountNumber(), e);
        }
        return newAccountNumber;
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

    public AccountData getAccountInformationByAccountNumber(String accountNumber) {
        Account accountEntity = accountRepository.findAccountInformationByAccountNumber(accountNumber);
        AccountData accountData = new AccountData();
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

    public AccountData getAccountInformationByCustomerId(Long customerId) {
        Integer customerIdAsInteger = customerId.intValue();
        Optional<Account> optionalAccount = accountRepository.findById(customerIdAsInteger);
        if (optionalAccount.isPresent()) {
            Account accountEntity = optionalAccount.get();
            return AccountData.builder()
                    .customerId(accountEntity.getCustomerId())
                    .accountHolderName(accountEntity.getAccountHolderName())
                    .accountNumber(accountEntity.getAccountNumber())
                    .createdAt(accountEntity.getCreatedAt())
                    .currentBalance(accountEntity.getCurrentBalance())
                    .currency(accountEntity.getCurrency())
                    .taxId(accountEntity.getTaxId())
                    .build();
        } else {
            return null;
        }
    }

    private Long generateUniqueCustomerId() {
        long customerId;
        do {
            customerId = accountUtilities.generateCustomerId(9);
        } while (accountRepository.existsById(Math.toIntExact(customerId)));
        return customerId;
    }
}
