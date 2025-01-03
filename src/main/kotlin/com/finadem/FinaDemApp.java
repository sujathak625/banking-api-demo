package com.finadem;

import com.finadem.entity.Account;
import com.finadem.enums.*;
import com.finadem.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication
public class FinaDemApp {
    public static void main(String[] args) {
        SpringApplication.run(FinaDemApp.class, args);
    }

    @Bean
    public CommandLineRunner loadMockTransactions(AccountRepository accountRepository) {
        return args -> {
            String iban = "DE89370400440532013000";

            if (accountRepository.count() == 0) {
                accountRepository.save(
                        Account.builder()
                                .customerId(1L)
                                .iban(iban)
                                .accountHolderName("Sujatha Rajesh")
                                .bic("DEUTDEFF")
                                .taxId("TAX123456")
                                .currentBalance(BigDecimal.valueOf(1500.00))
                                .currency(CurrencyEnum.EUR)
                                .status(AccountStatus.ACTIVE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                );
            } else {
                System.out.println("Account data already exists. Skipping initialization.");
            }
        };
    }
}
