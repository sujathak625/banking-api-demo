package com.finadem.service;

import com.finadem.entity.Account;
import com.finadem.exception.exceptions.AccountCreationFailedException;
import com.finadem.exception.exceptions.AccountDataUpdateFailedException;
import com.finadem.exception.exceptions.InvalidIbanException;
import com.finadem.request.AccountDataRequest;
import com.finadem.repository.AccountRepository;
import com.finadem.helper.AccountHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface AccountService {
    String createNewAccount(AccountDataRequest accountDataRequest);

    void updateAccountBalance(String accountNumber, BigDecimal accountBalance);

    AccountDataRequest getAccountInformationByAccountNumber(String accountNumber);

}

@Service
class AccountServiceImpl implements AccountService {
    Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;
    private final AccountHelper accountHelper;
    final String UNKNOWN = "Unknown";

    public AccountServiceImpl(AccountRepository accountRepository, AccountHelper accountHelper) {
        this.accountRepository = accountRepository;
        this.accountHelper = accountHelper;
    }

    public String createNewAccount(AccountDataRequest accountDataRequest) {
        String newAccountNumber;
        boolean isExists = accountRepository.findAccountInformationByAccountNumber(accountDataRequest.getIban()) != null;
        Account accountEntity = new Account();
        if (!isExists) {
            if (accountDataRequest.getCustomerId() == null) {
                accountEntity.setCustomerId(accountHelper.generateCustomerId(9));
            } else {
                accountEntity.setCustomerId(accountDataRequest.getCustomerId());
            }
            if (accountDataRequest.getIban() == null) {
                accountEntity.setIban(accountHelper.generateIBAN());
            } else {
                accountEntity.setIban(accountDataRequest.getIban());
            }
            accountEntity.setAccountHolderName(UNKNOWN + "-" + accountDataRequest.getAccountHolderName());
            if (accountDataRequest.getTaxId() == null) {
                accountEntity.setTaxId(UNKNOWN);
            } else {
                accountEntity.setTaxId(accountDataRequest.getTaxId());
            }
            accountEntity.setCurrentBalance(accountDataRequest.getCurrentBalance());
            accountEntity.setBic(accountHelper.getBic());
            accountEntity.setCurrency(accountDataRequest.getCurrency());
            if (accountDataRequest.getStatus() != null) {
                accountEntity.setStatus(accountDataRequest.getStatus());
            }
        }
        try {
            accountRepository.save(accountEntity);
            newAccountNumber = accountEntity.getIban();
        } catch (AccountCreationFailedException e) {
            logger.error("Error while creating account for account number {}", accountDataRequest.getIban(), e);
            throw new AccountCreationFailedException("Error while creating account for account number: " + accountDataRequest.getIban());
        }
        return newAccountNumber;
    }

    @Override
    public void updateAccountBalance(String accountNumber, BigDecimal accountBalance) {
        try {
            com.finadem.entity.Account account = accountRepository.findAccountInformationByAccountNumber(accountNumber);
            if (account == null) {
                logger.error("Account with account number {} not found", accountNumber);
                throw new InvalidIbanException("Account with account number {} not found", accountNumber);
            }
            account.setCurrentBalance(accountBalance);
            accountRepository.save(account);
        } catch (AccountDataUpdateFailedException e) {
            logger.error("Error while updating account balance for account number {}", accountNumber, e);
            throw new AccountDataUpdateFailedException("Error while updating account balance for account number: " + accountNumber);
        }
    }

    public AccountDataRequest getAccountInformationByAccountNumber(String accountNumber) {
        com.finadem.entity.Account accountEntity = accountRepository.findAccountInformationByAccountNumber(accountNumber);
        AccountDataRequest accountDataRequest = new AccountDataRequest();
        if (accountEntity != null) {
            accountDataRequest.setCustomerId(accountEntity.getCustomerId());
            accountDataRequest.setAccountHolderName(accountEntity.getAccountHolderName());
            accountDataRequest.setIban(accountEntity.getIban());
            accountDataRequest.setCurrentBalance(accountEntity.getCurrentBalance());
            accountDataRequest.setCurrency(accountEntity.getCurrency());
            accountDataRequest.setTaxId(accountEntity.getTaxId());
            accountDataRequest.setStatus(accountEntity.getStatus());
        } else {
            return null;
        }
        return accountDataRequest;
    }
}
