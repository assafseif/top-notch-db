package com.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_tile", schema = "dbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryTile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String link;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;
}
