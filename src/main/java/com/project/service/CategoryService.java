package com.project.service;

import com.project.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategory(Long id);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
}

