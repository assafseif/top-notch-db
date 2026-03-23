package com.project.controller;

import com.project.entity.Category;
import com.project.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllCategories() {
        logger.info("GET /api/categories - start");
        try {
            return categoryService.getAllCategories();
        } finally {
            logger.debug("GET /api/categories - end");
        }
    }

    @GetMapping("/{id}")
    public Category getCategory(@PathVariable Long id) {
        logger.info("GET /api/categories/{} - start", id);
        try {
            return categoryService.getCategory(id);
        } finally {
            logger.debug("GET /api/categories/{} - end", id);
        }
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        logger.info("POST /api/categories - start - name={}", category != null ? category.getName() : null);
        try {
            return categoryService.createCategory(category);
        } finally {
            logger.debug("POST /api/categories - end");
        }
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        logger.info("PUT /api/categories/{} - start - name={}", id, category != null ? category.getName() : null);
        try {
            return categoryService.updateCategory(id, category);
        } finally {
            logger.debug("PUT /api/categories/{} - end", id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        logger.info("DELETE /api/categories/{} - start", id);
        try {
            categoryService.deleteCategory(id);
        } finally {
            logger.debug("DELETE /api/categories/{} - end", id);
        }
    }
}
