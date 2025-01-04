package com.finadem.controller;

import com.finadem.request.AccountDataRequest;
import com.finadem.response.AccountDataResponse;
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

    /**
     * Fetches the account balance for the specified account number.
     *
     * @param accountNumber the unique identifier of the account
     * @return ResponseEntity containing account details (balance and currency) if the account exists,
     *         otherwise returns a NOT_FOUND response.
     */
    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNumber) {
        AccountDataRequest accountDataRequest = accountService.getAccountInformationByAccountNumber(accountNumber);
        if (accountDataRequest != null) {
            AccountDataResponse accountDataResponse = new AccountDataResponse();
            accountDataResponse.setAccountNumber(accountNumber);
            accountDataResponse.setCurrentBalance(accountDataRequest.getCurrentBalance());
            accountDataResponse.setCurrency(accountDataRequest.getCurrency());
            return ResponseEntity.ok(accountDataResponse);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account does not exist");
        }
    }

    // Only for testing purpose to test whether account creation works
   @PostMapping("/createAccount")
    public ResponseEntity<String> createAccount(@RequestBody AccountDataRequest accountDataRequest) {
        String newAccountNumber = accountService.createNewAccount(accountDataRequest);
        if (newAccountNumber!=null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account creation failed.");
        }
    }
}
