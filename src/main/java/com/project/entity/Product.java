package com.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

import lombok.*;

@Entity
@Table(name = "product", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

        private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "brand_id")
        @ToString.Exclude
        private Brand brand;

    @Column(nullable = false)
    private double price;

    private Double originalPrice;

    // single primary image blob (kept for backward compatibility / quick access)
    @Lob
    @Column(name = "image", columnDefinition = "VARBINARY(MAX)")
    @JsonIgnore // don't include raw blob in JSON responses to avoid huge payloads and serialization issues
    private byte[] imageBlob;

    // store multiple images as separate entities (Image has its own id). Use a join table so Image rows don't contain product_id
    @ManyToMany
    @JoinTable(
            name = "product_images",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id")
    )
    @JsonIgnore
    private List<Image> images;

    @Column(length = 2000)
    private String description;

        @Column(nullable = false)
        private String gender;

    private double rating;
    private int reviews;

    // sizes as reusable entities referenced by id via a join table
    @ManyToMany
    @JoinTable(
            name = "product_sizes",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "size_id")
    )
    private List<Size> sizes;

    // colors as reusable entities referenced by id via a join table
    @ManyToMany
    @JoinTable(
            name = "product_colors",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "color_id")
    )
    private List<Color> colors;

    // tags as reusable entities referenced by id via a join table
    @ManyToMany
    @JoinTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    private boolean isNew;
    private boolean isLimited;
    private boolean isBestseller;

    // keep minimal explicit methods if needed by JPA consumers; Lombok generated getters/setters are present
}
