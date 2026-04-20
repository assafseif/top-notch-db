package com.project.service;

import com.project.dto.SubcategoryRequest;
import com.project.entity.Category;
import com.project.entity.Subcategory;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategory(Long id);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
    Subcategory createSubcategory(Long categoryId, SubcategoryRequest request);
    Subcategory updateSubcategory(Long subcategoryId, SubcategoryRequest request);
    void deleteSubcategory(Long subcategoryId);
}

