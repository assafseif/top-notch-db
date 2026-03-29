package com.project.service;

import java.util.List;

public interface BrandService {
    List<String> getAllBrands();
    String createBrand(String name);
    long renameBrand(String currentName, String newName);
}