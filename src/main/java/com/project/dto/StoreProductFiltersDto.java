package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreProductFiltersDto {
    private Double minPrice;
    private Double maxPrice;
    private List<String> genders;
    private List<String> brands;
    private List<String> sizes;
    private List<String> colors;
    private List<String> features;
    private List<String> sortOptions;
}