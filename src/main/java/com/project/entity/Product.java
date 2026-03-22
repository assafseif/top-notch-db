package com.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "product", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private double price;

    private Double originalPrice;

    // single primary image blob (kept for backward compatibility / quick access)
    @Lob
    @Column(name = "image", columnDefinition = "VARBINARY(MAX)")
    @JsonIgnore // don't include raw blob in JSON responses to avoid huge payloads and serialization issues
    private byte[] imageBlob;

    // store multiple image blobs (each entry is VARBINARY(MAX)) in a separate collection table
    @ElementCollection
    @CollectionTable(name = "product_image_blobs", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_blob", columnDefinition = "VARBINARY(MAX)")
    @Lob
    @JsonIgnore
    private List<byte[]> imagesBlobs;

    @Column(length = 2000)
    private String description;

    private double rating;
    private int reviews;

    @ElementCollection
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    private List<String> sizes;

    @ElementCollection
    @CollectionTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color")
    private List<String> colors;

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags;

    private boolean isNew;
    private boolean isLimited;
    private boolean isBestseller;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    public byte[] getImageBlob() { return imageBlob; }
    public void setImageBlob(byte[] imageBlob) { this.imageBlob = imageBlob; }
    public List<byte[]> getImagesBlobs() { return imagesBlobs; }
    public void setImagesBlobs(List<byte[]> imagesBlobs) { this.imagesBlobs = imagesBlobs; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getReviews() { return reviews; }
    public void setReviews(int reviews) { this.reviews = reviews; }
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }
    public boolean isLimited() { return isLimited; }
    public void setLimited(boolean limited) { isLimited = limited; }
    public boolean isBestseller() { return isBestseller; }
    public void setBestseller(boolean bestseller) { isBestseller = bestseller; }
}
