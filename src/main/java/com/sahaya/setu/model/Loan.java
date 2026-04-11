package com.sahaya.setu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private Double principalAmount;
    private Double interestRate;
    private Double monthlyInstallment;
    private Double outstandingBalance;
    private String status = "ACTIVE"; // "ACTIVE", "CLEARED", "DEFAULTED"
    private LocalDate disbursementDate = LocalDate.now();
}
