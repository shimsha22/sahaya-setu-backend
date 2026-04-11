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
    private Double totalCorpus = 0.0;
}
