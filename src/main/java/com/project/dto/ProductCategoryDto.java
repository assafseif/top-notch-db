package com.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductCategoryDto {
    private Long id;
    private String name;
    private double price;
    private Long categoryId;
    private String categoryName;
}

