package com.finadem.controller;

import com.finadem.dto.AccountDTO;
import com.finadem.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/${api.version}/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNumber) {
        AccountDTO accountData = accountService.getAccountInformation(accountNumber);
        if (accountData != null) {
            return ResponseEntity.ok(accountData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account does not exist");
        }
    }

    @PostMapping("/createAccount")
    public ResponseEntity<String> createAccount(@RequestBody AccountDTO account) {
        boolean accountCreationStatus = accountService.createNewAccount(account);
        if (accountCreationStatus) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account creation failed.");
        }
    }

    @PostMapping("/updateBalance/{accountNumber}/{transactionAmount}/{transactionType}")
    public ResponseEntity<String> updateAccountBalance(@PathVariable String accountNumber, @PathVariable BigDecimal accountBalance,
                                                       @PathVariable String transactionType) {
        return ResponseEntity.status(HttpStatus.OK).body("Account balance updated successfully.");
    }
}
