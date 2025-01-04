package com.finadem.service;

import com.finadem.entity.Account;
import com.finadem.enums.AccountStatus;
import com.finadem.enums.CurrencyEnum;
import com.finadem.exception.exceptions.AccountCreationFailedException;
import com.finadem.exception.exceptions.InvalidIbanException;
import com.finadem.helper.AccountHelper;
import com.finadem.repository.AccountRepository;
import com.finadem.request.AccountDataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountHelper accountHelper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createNewAccount_ShouldCreateAccount_WhenValidRequest() {
        AccountDataRequest request = new AccountDataRequest();
        request.setIban(null);
        request.setCustomerId(null);
        request.setAccountHolderName("John Doe");
        request.setCurrentBalance(new BigDecimal("1000.00"));
        request.setCurrency(CurrencyEnum.EUR);
        request.setTaxId("TAX1234");

        Account account = new Account();
        account.setIban("DE89370400440532013000");
        when(accountRepository.findAccountInformationByAccountNumber(null)).thenReturn(null);
        when(accountHelper.generateIBAN()).thenReturn("DE89370400440532013000");
        when(accountHelper.getBic()).thenReturn("BIC123");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            savedAccount.setIban("DE89370400440532013000");
            return savedAccount;
        });

        String iban = accountService.createNewAccount(request);

        assertEquals("DE89370400440532013000", iban);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createNewAccount_ShouldThrowException_WhenAccountCreationFails() {
        AccountDataRequest request = new AccountDataRequest();
        request.setIban(null);
        request.setCustomerId(null);
        request.setAccountHolderName("John Doe");
        request.setCurrentBalance(new BigDecimal("1000.00"));
        request.setCurrency(CurrencyEnum.EUR);
        request.setTaxId("TAX1234");

        when(accountRepository.findAccountInformationByAccountNumber(null)).thenReturn(null);
        when(accountHelper.generateIBAN()).thenReturn("IBAN123456789");
        doThrow(new AccountCreationFailedException("Database error")).when(accountRepository).save(any(Account.class));
        assertThrows(AccountCreationFailedException.class, () -> accountService.createNewAccount(request));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void updateAccountBalance_ShouldUpdateBalance_WhenAccountExists() {
        Account account = new Account();
        account.setIban("DE89370400440532013000");
        account.setCurrentBalance(new BigDecimal("500.00"));

        when(accountRepository.findAccountInformationByAccountNumber("DE89370400440532013000")).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.updateAccountBalance("DE89370400440532013000", new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("1000.00"), account.getCurrentBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void updateAccountBalance_ShouldThrowException_WhenAccountDoesNotExist() {
        when(accountRepository.findAccountInformationByAccountNumber("INVALID_IBAN")).thenReturn(null);
        assertThrows(InvalidIbanException.class, () -> accountService.updateAccountBalance("INVALID_IBAN", new BigDecimal("1000.00")));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountInformationByAccountNumber_ShouldReturnAccountData_WhenAccountExists() {
        Account account = new Account();
        account.setCustomerId(12345L);
        account.setAccountHolderName("John Doe");
        account.setIban("DE89370400440532013000");
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setCurrency(CurrencyEnum.EUR);
        account.setTaxId("TAX123");
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findAccountInformationByAccountNumber("DE89370400440532013000")).thenReturn(account);

        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber("DE89370400440532013000");

        assertNotNull(accountDataRequest);
        assertEquals("John Doe", accountDataRequest.getAccountHolderName());
        assertEquals("DE89370400440532013000", accountDataRequest.getIban());
        assertEquals(new BigDecimal("1000.00"), accountDataRequest.getCurrentBalance());
        assertEquals(CurrencyEnum.EUR, accountDataRequest.getCurrency());
        verify(accountRepository, times(1)).findAccountInformationByAccountNumber("DE89370400440532013000");
    }

    @Test
    void getAccountInformationByAccountNumber_ShouldReturnNull_WhenAccountDoesNotExist() {
        when(accountRepository.findAccountInformationByAccountNumber("INVALID_IBAN")).thenReturn(null);

        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber("INVALID_IBAN");

        assertNull(accountDataRequest);
        verify(accountRepository, times(1)).findAccountInformationByAccountNumber("INVALID_IBAN");
    }
}
