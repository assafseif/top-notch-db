package com.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "loyalty_section", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoyaltySection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String heading;
    private String buttonText;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_section_id")
    private List<LoyaltyPerk> perks;
}
