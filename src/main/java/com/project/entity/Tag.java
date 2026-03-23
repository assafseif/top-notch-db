package com.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Tag(String name) {
        this.name = name;
    }
}

