package com.project.controller;

import com.project.entity.Category;
import com.project.repository.CategoryRepository;
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
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAllCategories() {
        logger.info("GET /api/categories - start");
        try {
            List<Category> all = categoryRepository.findAll();
            logger.info("GET /api/categories - found {} categories", all.size());
            return all;
        } catch (Exception e) {
            logger.error("GET /api/categories - error", e);
            throw e;
        } finally {
            logger.debug("GET /api/categories - end");
        }
    }

    @GetMapping("/{id}")
    public Category getCategory(@PathVariable Long id) {
        logger.info("GET /api/categories/{} - start", id);
        try {
            Category c = categoryRepository.findById(id).orElse(null);
            logger.info("GET /api/categories/{} - result={}", id, (c != null));
            return c;
        } catch (Exception e) {
            logger.error("GET /api/categories/{} - error", id, e);
            throw e;
        } finally {
            logger.debug("GET /api/categories/{} - end", id);
        }
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        logger.info("POST /api/categories - start - name={}", category != null ? category.getName() : null);
        try {
            Category saved = categoryRepository.save(category);
            logger.info("POST /api/categories - saved id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("POST /api/categories - error", e);
            throw e;
        } finally {
            logger.debug("POST /api/categories - end");
        }
    }

    @PutMapping("/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        logger.info("PUT /api/categories/{} - start - name={}", id, category != null ? category.getName() : null);
        try {
            category.setId(id);
            Category updated = categoryRepository.save(category);
            logger.info("PUT /api/categories/{} - saved", id);
            return updated;
        } catch (Exception e) {
            logger.error("PUT /api/categories/{} - error", id, e);
            throw e;
        } finally {
            logger.debug("PUT /api/categories/{} - end", id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        logger.info("DELETE /api/categories/{} - start", id);
        try {
            categoryRepository.deleteById(id);
            logger.info("DELETE /api/categories/{} - deleted", id);
        } catch (Exception e) {
            logger.error("DELETE /api/categories/{} - error", id, e);
            throw e;
        } finally {
            logger.debug("DELETE /api/categories/{} - end", id);
        }
    }
}
