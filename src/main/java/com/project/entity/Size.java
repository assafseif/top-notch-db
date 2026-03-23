package com.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "size", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String value;

    public Size(String value) {
        this.value = value;
    }
}

