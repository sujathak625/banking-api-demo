package com.finadem.entity;

import com.finadem.enums.CurrencyEnum;
import com.finadem.enums.TransactionStatus;
import com.finadem.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(nullable = false, length = 25)
    private String customerAccount;

    @Column(nullable = false, length = 25)
    private String transactingAccount;

    @Column
    private String bic;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionStatus status;

    @Column
    private String transactionRemarks;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private CurrencyEnum currency = CurrencyEnum.EUR;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
