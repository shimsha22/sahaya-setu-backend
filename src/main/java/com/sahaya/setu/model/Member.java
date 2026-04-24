package com.sahaya.setu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum Role {
        PRESIDENT,
        SECRETARY,
        MEMBER
    }

    // We use FetchType.LAZY so Spring Boot doesn't pull the entire Group
    // out of the database every single time it looks at a Member. It saves memory!
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shg_id", nullable = false)
    private ShgGroup shgGroup;

    private String fullName;
    private String password;

    // We make this UNIQUE because it is our "sahaya_identifier" for logging in.
    @Column(unique = true, nullable = false)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;

    private String pinCode;

    // --- FINTECH DOMAIN FIELDS ---

    // The total amount this member has contributed to the pool
    private Double totalSavedShare = 0.0;

    // NEW: The current active debt this member owes to the group.
    // If you remember the React Profile UI we built, it showed "Loan Balance" in red.
    // This is the variable that feeds that exact UI!
    private Double totalLoanOutstanding = 0.0;

    // Evaluators will love this: "A localized trust metric mimicking traditional credit bureaus"
    private Integer credibilityScore = 300;
}