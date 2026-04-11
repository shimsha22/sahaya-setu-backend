package com.sahaya.setu.model;

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

    @ManyToOne
    @JoinColumn(name = "shg_id")
    private ShgGroup shgGroup;

    private String fullName;
    private String password;
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    private Role role =Role.MEMBER;;

    private String pinCode;

    private Double totalSavedShare = 0.0;
    private Integer credibilityScore = 300;
}
