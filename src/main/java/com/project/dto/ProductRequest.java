package com.project.dto;

import java.util.List;

public class ProductRequest {
    public String name;
    public Double price;
    public Double originalPrice;
    public String categoryName; // accept category by name for frontend convenience
    public Long categoryId; // or by id
    public Object category; // flexible: can be String (name) or object {id:.., name:..}
    public List<String> imagesBase64;
    public String description;
    public Double rating;
    public Integer reviews;
    public List<String> sizes;
    public List<String> colors;
    public List<String> tags;
    public Boolean isNew;
    public Boolean isLimited;
    public Boolean isBestseller;
}
