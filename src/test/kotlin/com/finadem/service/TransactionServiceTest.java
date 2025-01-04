package com.finadem.service;

import com.finadem.entity.Transaction;
import com.finadem.enums.*;
import com.finadem.exception.exceptions.*;
import com.finadem.repository.TransactionRepository;
import com.finadem.request.AccountDataRequest;
import com.finadem.request.DepositWithdrawalRequest;
import com.finadem.request.FundTransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.NoTransactionException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private CurrencyConverterService currencyConverterService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLastNTransactionHistory_ShouldReturnTransactions_WhenTransactionsExist() {
        // Arrange
        String iban = "IBAN123";
        int lastNTransactions = 5;
        Pageable pageable = PageRequest.of(0, lastNTransactions);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction());
        when(transactionRepository.getTransactionByAccountNumber(iban, pageable)).thenReturn(new PageImpl<>(transactions));

        // Act
        List<Transaction> result = transactionService.getLastNTransactionHistory(iban, lastNTransactions);

        // Assert
        assertEquals(1, result.size());
        verify(transactionRepository, times(1)).getTransactionByAccountNumber(iban, pageable);
    }

    @Test
    void getLastNTransactionHistory_ShouldThrowException_WhenNoTransactionsExist() {
        // Arrange
        String iban = "IBAN123";
        int lastNTransactions = 5;
        Pageable pageable = PageRequest.of(0, lastNTransactions);
        when(transactionRepository.getTransactionByAccountNumber(iban, pageable)).thenReturn(new PageImpl<>(new ArrayList<>()));

        // Act & Assert
        assertThrows(NoTransactionException.class, () -> transactionService.getLastNTransactionHistory(iban, lastNTransactions));
    }

    @Test
    void createDepositTransaction_ShouldCreateTransaction_WhenValidRequest() {
        // Arrange
        String iban = "IBAN123";
        BigDecimal amount = new BigDecimal("100.00");
        AccountDataRequest accountDataRequest = new AccountDataRequest();
        accountDataRequest.setCurrentBalance(new BigDecimal("500.00"));
        when(accountService.getAccountInformationByAccountNumber(iban)).thenReturn(accountDataRequest);

        // Act
        transactionService.createDepositTransaction(iban, CurrencyEnum.EUR, amount, "Deposit Test", TransactionType.DEPOSIT, TransactionSource.ATM);

        // Assert
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountService, times(1)).updateAccountBalance(iban, new BigDecimal("600.00"));
    }

    @Test
    void createWithdrawalTransaction_ShouldCreateTransaction_WhenSufficientBalance() {
        // Arrange
        DepositWithdrawalRequest withdrawalRequest = new DepositWithdrawalRequest();
        withdrawalRequest.setIban("IBAN123");
        withdrawalRequest.setAmount("100.00");
        withdrawalRequest.setCurrency(CurrencyEnum.EUR);
        withdrawalRequest.setTransactionSource(TransactionSource.ATM);
        withdrawalRequest.setTransactionType(TransactionType.WITHDRAWAL);

        AccountDataRequest accountDataRequest = new AccountDataRequest();
        accountDataRequest.setCurrentBalance(new BigDecimal("500.00"));
        when(accountService.getAccountInformationByAccountNumber("IBAN123")).thenReturn(accountDataRequest);

        // Act
        transactionService.createWithdrawalTransaction(withdrawalRequest);

        // Assert
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountService, times(1)).updateAccountBalance("IBAN123", new BigDecimal("400.00"));
    }

    @Test
    void createWithdrawalTransaction_ShouldThrowException_WhenInsufficientBalance() {
        // Arrange
        DepositWithdrawalRequest withdrawalRequest = new DepositWithdrawalRequest();
        withdrawalRequest.setIban("IBAN123");
        withdrawalRequest.setAmount("1000.00");
        withdrawalRequest.setCurrency(CurrencyEnum.EUR);
        withdrawalRequest.setTransactionSource(TransactionSource.ATM);
        withdrawalRequest.setTransactionType(TransactionType.WITHDRAWAL);

        AccountDataRequest accountDataRequest = new AccountDataRequest();
        accountDataRequest.setCurrentBalance(new BigDecimal("500.00"));
        when(accountService.getAccountInformationByAccountNumber("IBAN123")).thenReturn(accountDataRequest);

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> transactionService.createWithdrawalTransaction(withdrawalRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createFundTransferTransaction_ShouldCreateTransactions_WhenValidRequest() {
        // Arrange
        FundTransferRequest fundTransferRequest = new FundTransferRequest();
        fundTransferRequest.setCustomerAccountNumber("IBAN123");
        fundTransferRequest.setTransactingAccountNumber("IBAN456");
        fundTransferRequest.setAmount("100.00");
        fundTransferRequest.setCurrencyType(CurrencyEnum.EUR);
        fundTransferRequest.setTransactionType(TransactionType.DEBIT);

        AccountDataRequest senderAccount = new AccountDataRequest();
        senderAccount.setCurrentBalance(new BigDecimal("500.00"));
        AccountDataRequest recipientAccount = new AccountDataRequest();
        recipientAccount.setCurrentBalance(new BigDecimal("300.00"));

        when(accountService.getAccountInformationByAccountNumber("IBAN123")).thenReturn(senderAccount);
        when(accountService.getAccountInformationByAccountNumber("IBAN456")).thenReturn(recipientAccount);

        // Act
        transactionService.createFundTransferTransaction(fundTransferRequest);

        // Assert
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountService, times(1)).updateAccountBalance("IBAN123", new BigDecimal("400.00"));
        verify(accountService, times(1)).updateAccountBalance("IBAN456", new BigDecimal("400.00"));
    }

    @Test
    void createFundTransferTransaction_ShouldThrowException_WhenSenderAndReceiverAreSame() {
        // Arrange
        FundTransferRequest fundTransferRequest = new FundTransferRequest();
        fundTransferRequest.setCustomerAccountNumber("IBAN123");
        fundTransferRequest.setTransactingAccountNumber("IBAN123");
        fundTransferRequest.setAmount("100.00");
        fundTransferRequest.setCurrencyType(CurrencyEnum.EUR);
        fundTransferRequest.setTransactionType(TransactionType.DEBIT);

        // Act & Assert
        assertThrows(TransferToSelfException.class, () -> transactionService.createFundTransferTransaction(fundTransferRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
