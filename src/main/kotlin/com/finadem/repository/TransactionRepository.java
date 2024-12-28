package com.finadem.repository;

import com.finadem.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("FROM Transaction t WHERE t.iban = :iban ORDER BY t.timestamp DESC")
    Page<Transaction> getTransactionByAccountNumber(@Param("iban") String iban, Pageable pageable);

    @Query("FROM Transaction t WHERE t.iban = :iban AND t.timestamp BETWEEN :startDate AND :endDate ORDER BY t.timestamp DESC")
    List<Transaction> getTransactionHistoryBetween(
            @Param("iban") String iban,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
