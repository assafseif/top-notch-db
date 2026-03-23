package com.project.dto;

import com.project.entity.Category;
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
    private double price;
    private Double originalPrice;
    private String description;
    private Double rating;
    private Integer reviews;
    private Integer quantity;
    private List<String> sizes;
    private List<String> colors;
    private List<String> tags;
    private Boolean isNew;
    private Boolean isLimited;
    private Boolean isBestseller;

    // primary image as base64 string (data URL safe)
    private String imageBase64;
    // additional images as base64 strings
    private List<String> imagesBase64;
    private Category category;

//    private Long categoryId;
//    private String categoryName;
}

