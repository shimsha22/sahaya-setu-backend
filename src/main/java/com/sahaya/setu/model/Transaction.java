package com.sahaya.setu.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ShgGroup group;

    // Optional: If this transaction was paying off a loan, link it here!
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan relatedLoan;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Double totalAmount;       // The total cash handed over (e.g., 1000)
    private Double principalPortion;  // How much paid off debt (e.g., 900)
    private Double interestPortion;   // How much was group profit (e.g., 100)

    private LocalDateTime timestamp;

    public enum TransactionType {
        SAVINGS_DEPOSIT, // Normal monthly saving
        LOAN_DISBURSEMENT, // Group giving money OUT
        LOAN_REPAYMENT,  // Member giving money IN (paying debt)
        WITHDRAWAL // Member taking savings out
    }
}