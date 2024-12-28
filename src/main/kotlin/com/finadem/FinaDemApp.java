package com.finadem;

import com.finadem.entity.Transaction;
import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionSource;
import com.finadem.enums.TransactionStatus;
import com.finadem.enums.TransactionType;
import com.finadem.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.IntStream;

@SpringBootApplication
public class FinaDemApp {
    public static void main(String[] args) {
        SpringApplication.run(FinaDemApp.class, args);
    }

    @Bean
    public CommandLineRunner loadMockTransactions(TransactionRepository transactionRepository) {
        return args -> {
            String iban = "DE89370400440532013000";
            LocalDateTime startDate = LocalDateTime.of(2024, 11, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 11, 30, 23, 59);
            Random random = new Random();

            IntStream.range(0, 10).forEach(i -> {
                Transaction transaction = new Transaction();
                transaction.setIban(iban);
                transaction.setAmount(BigDecimal.valueOf(50 + random.nextDouble() * 500));
                transaction.setCurrency(CurrencyEnum.USD);
                transaction.setType(TransactionType.DEPOSIT);
                transaction.setSource(TransactionSource.BANK_COUNTER);
                transaction.setTransactionRemarks("Mock transaction " + (i + 1));
                transaction.setTimestamp(generateRandomDateTime(startDate, endDate, random));
                transaction.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.save(transaction);
            });

            IntStream.range(0, 10).forEach(i -> {
                Transaction transaction = new Transaction();
                transaction.setIban(iban);
                transaction.setAmount(BigDecimal.valueOf(50 + random.nextDouble() * 500));
                transaction.setCurrency(CurrencyEnum.USD);
                transaction.setType(TransactionType.WITHDRAWAL);
                transaction.setSource(TransactionSource.ATM);
                transaction.setTransactionRemarks("Mock transaction " + (i + 1));
                transaction.setTimestamp(generateRandomDateTime(startDate, endDate, random));
                transaction.setStatus(TransactionStatus.SUCCESS);
                transactionRepository.save(transaction);
            });

            System.out.println("Mock transactions inserted successfully.");
        };
    }

    private LocalDateTime generateRandomDateTime(LocalDateTime startDate, LocalDateTime endDate, Random random) {
        long startEpoch = startDate.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = endDate.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = startEpoch + (long) (random.nextDouble() * (endEpoch - startEpoch));
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }
}
