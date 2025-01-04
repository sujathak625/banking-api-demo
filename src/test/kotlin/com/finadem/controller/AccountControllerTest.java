package com.finadem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finadem.enums.CurrencyEnum;
import com.finadem.request.AccountDataRequest;
import com.finadem.response.AccountDataResponse;
import com.finadem.service.AccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAccountBalance_AccountExists() throws Exception {
        AccountDataRequest accountDataRequest = new AccountDataRequest();
        accountDataRequest.setIban("123456789");
        accountDataRequest.setCurrentBalance(new BigDecimal("1000.00"));
        accountDataRequest.setCurrency(CurrencyEnum.EUR);

        Mockito.when(accountService.getAccountInformationByAccountNumber("123456789"))
                .thenReturn(accountDataRequest);

        mockMvc.perform(get("/api/v1/accounts/balance/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123456789"))
                .andExpect(jsonPath("$.currentBalance").value(1000.00))
                .andExpect(jsonPath("$.currency").value(CurrencyEnum.EUR.toString()));
    }

    @Test
    void testGetAccountBalance_AccountDoesNotExist() throws Exception {
        Mockito.when(accountService.getAccountInformationByAccountNumber("123456789"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/accounts/balance/123456789"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Account does not exist"));
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        AccountDataRequest accountDataRequest = new AccountDataRequest();
        accountDataRequest.setIban("123456789");
        accountDataRequest.setCurrency(CurrencyEnum.EUR);
        accountDataRequest.setCurrentBalance(new BigDecimal("5000.00"));

        Mockito.when(accountService.createNewAccount(Mockito.any(AccountDataRequest.class)))
                .thenReturn("123456789");

        mockMvc.perform(post("/api/v1/accounts/createAccount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDataRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Account created successfully."));
    }

    @Test
    void testCreateAccount_Failure() throws Exception {
        AccountDataRequest accountDataRequest = new AccountDataRequest();
        accountDataRequest.setIban("123456789");
        accountDataRequest.setCurrency(CurrencyEnum.EUR);
        accountDataRequest.setCurrentBalance(new BigDecimal("5000.00"));

        Mockito.when(accountService.createNewAccount(Mockito.any(AccountDataRequest.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/v1/accounts/createAccount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDataRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Account creation failed."));
    }
}
