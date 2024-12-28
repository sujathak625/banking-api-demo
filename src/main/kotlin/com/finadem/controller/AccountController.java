package com.finadem.controller;

import com.finadem.model.AccountData;
import com.finadem.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNumber) {
        AccountData accountData = accountService.getAccountInformationByAccountNumber(accountNumber);
        if (accountData != null) {
            return ResponseEntity.ok(accountData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account does not exist");
        }
    }

    @PostMapping("/createAccount")
    public ResponseEntity<String> createAccount(@RequestBody AccountData account) {
        String newAccountNumber = accountService.createNewAccount(account);
        if (newAccountNumber!=null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account creation failed.");
        }
    }
}
