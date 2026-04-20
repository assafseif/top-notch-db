package com.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Entity
@Table(
        name = "subcategory",
        schema = "dbo",
        uniqueConstraints = @UniqueConstraint(name = "uk_subcategory_category_name", columnNames = {"category_id", "name"})
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Subcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Category category;

    @OneToMany(mappedBy = "subcategory")
    @JsonIgnore
    @ToString.Exclude
    private List<Product> products;
}