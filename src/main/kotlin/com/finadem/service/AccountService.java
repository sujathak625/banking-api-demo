package com.finadem.service;

import com.finadem.request.AccountDataRequest;
import com.finadem.repository.AccountRepository;
import com.finadem.helper.AccountHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountService {
    String createNewAccount(AccountDataRequest accountDataRequest);

    boolean updateAccountBalance(String accountNumber, BigDecimal accountBalance);

    AccountDataRequest getAccountInformationByAccountNumber(String accountNumber);

}

@Service
class AccountServiceImpl implements AccountService {

    Logger logger = LoggerFactory.getLogger(AccountService.class);
    AccountRepository accountRepository;
    private final AccountHelper accountHelper;

    final String UNKNOWN = "Unknown-";

    public AccountServiceImpl(AccountRepository accountRepository, AccountHelper accountHelper) {
        this.accountRepository = accountRepository;
        this.accountHelper = accountHelper;
    }

    public String createNewAccount(AccountDataRequest accountDataRequest) {
        String newAccountNumber = null;
        boolean isExists = accountRepository.findAccountInformationByAccountNumber(accountDataRequest.getAccountNumber()) != null;
        com.finadem.entity.Account accountEntity = new com.finadem.entity.Account();
        if (!isExists) {
            if (accountDataRequest.getCustomerId() == null) {
                accountEntity.setCustomerId(generateUniqueCustomerId());
            } else {
                accountEntity.setCustomerId(accountDataRequest.getCustomerId());
            }
            if (accountDataRequest.getAccountNumber() == null) {
                accountEntity.setIban(accountHelper.generateIBAN());
            } else {
                accountEntity.setIban(accountDataRequest.getAccountNumber());
            }
            accountEntity.setAccountHolderName(UNKNOWN + "-" + accountDataRequest.getAccountHolderName());
            if (accountDataRequest.getTaxId() == null) {
                accountEntity.setTaxId(UNKNOWN);
            } else {
                accountEntity.setTaxId(accountDataRequest.getTaxId());
            }
            accountEntity.setCurrentBalance(accountDataRequest.getCurrentBalance());
            accountEntity.setCurrency(accountDataRequest.getCurrency());
            if (accountDataRequest.getStatus() != null) {
                accountEntity.setStatus(accountDataRequest.getStatus());
            }
        }
        try {
            accountRepository.save(accountEntity);
            newAccountNumber = accountEntity.getIban();
        } catch (Exception e) {
            logger.error("Error while creating account for account number {}", accountDataRequest.getAccountNumber(), e);
        }
        return newAccountNumber;
    }

    @Override
    public boolean updateAccountBalance(String accountNumber, BigDecimal accountBalance) {
        try {
            com.finadem.entity.Account account = accountRepository.findAccountInformationByAccountNumber(accountNumber);
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

    public AccountDataRequest getAccountInformationByAccountNumber(String accountNumber) {
        com.finadem.entity.Account accountEntity = accountRepository.findAccountInformationByAccountNumber(accountNumber);
        AccountDataRequest accountDataRequest = new AccountDataRequest();
        if (accountEntity != null) {
            accountDataRequest.setCustomerId(accountEntity.getCustomerId());
            accountDataRequest.setAccountHolderName(accountEntity.getAccountHolderName());
            accountDataRequest.setAccountNumber(accountEntity.getIban());
            accountDataRequest.setCurrentBalance(accountEntity.getCurrentBalance());
            accountDataRequest.setCurrency(accountEntity.getCurrency());
            accountDataRequest.setTaxId(accountEntity.getTaxId());
            accountDataRequest.setStatus(accountEntity.getStatus());
        } else {
            return null;
        }
        return accountDataRequest;
    }

    public AccountDataRequest getAccountInformationByCustomerId(Long customerId) {
        Integer customerIdAsInteger = customerId.intValue();
        Optional<com.finadem.entity.Account> optionalAccount = accountRepository.findById(customerIdAsInteger);
        if (optionalAccount.isPresent()) {
            com.finadem.entity.Account accountEntity = optionalAccount.get();
            return AccountDataRequest.builder()
                    .customerId(accountEntity.getCustomerId())
                    .accountHolderName(accountEntity.getAccountHolderName())
                    .accountNumber(accountEntity.getIban())
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
            customerId = accountHelper.generateCustomerId(9);
        } while (accountRepository.existsById(Math.toIntExact(customerId)));
        return customerId;
    }
}
