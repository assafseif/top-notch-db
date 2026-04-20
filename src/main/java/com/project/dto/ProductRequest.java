package com.project.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductRequest {
    private String name;
    private String barcode;
    private String itemCode;
    private Double price;
    private Double originalPrice;
    private String categoryName;
    private Long categoryId;
    private String subcategoryName;
    private Long subcategoryId;
    private List<String> imagesBase64;
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
}
