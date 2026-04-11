package com.sahaya.setu.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shg_id")
    private ShgGroup shgGroup;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member; // Can be null if it's a group-wide expense

    private String transactionType; // "SAVINGS_DEPOSIT", "LOAN_REPAYMENT", "GROUP_EXPENSE"
    private Double amount;

    private String description;


    private Double principalPortion = 0.0;
    private Double interestPortion = 0.0;
    private Double penaltyAmount = 0.0;
    private LocalDateTime transactionDate = LocalDateTime.now();

    private String voiceCommandRaw;
    private Boolean isVerified = false;
}
