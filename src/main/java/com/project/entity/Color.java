package com.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "color", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String value;

    public Color(String value) {
        this.value = value;
    }
}

