package com.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loyalty_perk", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoyaltyPerk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String icon;
    private String title;
    private String description;
}
