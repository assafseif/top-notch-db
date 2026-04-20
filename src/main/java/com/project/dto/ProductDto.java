package com.project.dto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDto {
    private Long id;
    private String name;
    private String barcode;
    private String itemCode;
    private double price;
    private Double originalPrice;
    private String description;
    private String gender;
    private String brand;
    private Double rating;
    private Integer reviews;
    private Integer quantity;
    private List<String> sizes;
    private List<String> colors;
    private List<String> tags;
    private Boolean isNew;
    private Boolean isLimited;
    private Boolean isBestseller;

    // cacheable backend URL for the primary image
    private String imageUrl;
    // cacheable backend URLs for gallery images
    private List<String> imageUrls;

    // legacy fields kept for backwards compatibility
    private String imageBase64;
    private List<String> imagesBase64;
    private CategorySummaryDto category;
    private Long subcategoryId;
    private String subcategory;

//    private Long categoryId;
//    private String categoryName;
}

