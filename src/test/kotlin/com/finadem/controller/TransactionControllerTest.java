package com.finadem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finadem.entity.Transaction;
import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionSource;
import com.finadem.enums.TransactionType;
import com.finadem.request.DepositWithdrawalRequest;
import com.finadem.request.FundTransferRequest;
import com.finadem.service.TransactionService;
import com.finadem.helper.DateHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private DateHelper dateHelper;

    @Test
    void testDepositFund_Success() throws Exception {
        DepositWithdrawalRequest depositRequest = new DepositWithdrawalRequest();
        depositRequest.setIban("DE89370400440532013000");
        depositRequest.setCurrency(CurrencyEnum.EUR);
        depositRequest.setAmount("500.00");
        depositRequest.setTransactionRemarks("Test deposit");
        depositRequest.setTransactionType(TransactionType.DEPOSIT);
        depositRequest.setTransactionSource(TransactionSource.ATM);

        mockMvc.perform(post("/api/v1/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Deposit Successful."));
    }

    @Test
    void testWithdrawal_Success() throws Exception {
        DepositWithdrawalRequest withdrawalRequest = new DepositWithdrawalRequest();
        withdrawalRequest.setIban("DE89370400440532013000");
        withdrawalRequest.setCurrency(CurrencyEnum.EUR);
        withdrawalRequest.setAmount("200.00");
        withdrawalRequest.setTransactionRemarks("Test withdrawal");
        withdrawalRequest.setTransactionType(TransactionType.WITHDRAWAL);
        withdrawalRequest.setTransactionSource(TransactionSource.BANK_COUNTER);

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawalRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Withdrawal successful."));
    }

    @Test
    void testTransferFunds_Success() throws Exception {
        FundTransferRequest transferRequest = new FundTransferRequest();
        transferRequest.setTransactingAccountNumber("AD1099077818KGI8OKLCMG4Y");
        transferRequest.setTransactingAccountBIC("DEUTDEFF");
        transferRequest.setCustomerAccountNumber("DE89370400440532013000");
        transferRequest.setAmount("1000.00");
        transferRequest.setCurrencyType(CurrencyEnum.EUR);
        transferRequest.setTransactionType(TransactionType.CREDIT);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Fund Transfer successful."));
    }

    @Test
    void testGetLastNTransactions_Success() throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setIban("DE89370400440532013000");
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setType(TransactionType.DEPOSIT);
        transactions.add(transaction);

        when(transactionService.getLastNTransactionHistory(anyString(), anyInt())).thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions/history/DE89370400440532013000/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("DE89370400440532013000"))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void testGetTransactionHistoryBetween_Success() throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setIban("DE89370400440532013000");
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setType(TransactionType.CREDIT);
        transactions.add(transaction);

        when(dateHelper.validateAndParseDate(anyString(), any())).thenReturn(LocalDateTime.now().toLocalDate());
        when(transactionService.getTransactionHistoryBetween(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions/history/DE89370400440532013000/01-01-2025/31-12-2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].iban").value("DE89370400440532013000"))
                .andExpect(jsonPath("$[0].amount").value(1000.00))
                .andExpect(jsonPath("$[0].type").value("CREDIT"));
    }
}
