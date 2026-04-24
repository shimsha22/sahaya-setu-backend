package com.sahaya.setu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "loan")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Upgraded with LAZY fetching for performance
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // CRITICAL NEW FIELD: Links the loan to the specific SHG's money pool
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shg_id", nullable = false)
    private ShgGroup shgGroup;

    private Double principalAmount;

    // e.g., 2.0 for 2% per month
    private Double interestRate;

    // Kept your original field! Great for EMI tracking.
    private Double monthlyInstallment;

    private Double outstandingBalance;

    // Upgraded from String to strict Enum to prevent database corruption
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;

    private LocalDate disbursementDate = LocalDate.now();

    // The strict list of allowed statuses (kept your exact categories!)
    public enum LoanStatus {
        ACTIVE,
        CLEARED,
        DEFAULTED
    }
}