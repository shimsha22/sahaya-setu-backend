package com.sahaya.setu.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "shg_group")
@Getter
@Setter
@NoArgsConstructor
public class ShgGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String shgCode;

    private String shgName;

    private LocalDate formationDate = LocalDate.now();

    private Double baseMonthlySaving;



    // 1. The liquid cash in the box right now.
    // (Goes UP on deposits/interest paid. Goes DOWN on loan disbursements).
    private Double availableBalance = 0.0;

    // 2. The total value of the group (availableBalance + all active loan principals).
    // (This is the 'Corpus' the bank looks at for NRLM Credit Linkage).
    private Double totalGroupWealth = 0.0;

    // 3. (Optional but recommended) For the 2-month NRLM milestone "Savings A/C opening"
    private String bankAccountNumber;

    private boolean isBankAccountLinked = false;
}