package com.project.service.impl;

import com.project.entity.Category;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import com.project.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Category> getAllCategories() {
        logger.info("getAllCategories - start");
        try {
            List<Category> all = categoryRepository.findAll();
            logger.info("getAllCategories - found {} categories", all.size());
            return all;
        } catch (Exception e) {
            logger.error("getAllCategories - error", e);
            throw e;
        } finally {
            logger.debug("getAllCategories - end");
        }
    }

    @Override
    public Category getCategory(Long id) {
        logger.info("getCategory - start id={}", id);
        try {
            Category c = categoryRepository.findById(id).orElse(null);
            logger.info("getCategory - result={}", (c != null));
            return c;
        } catch (Exception e) {
            logger.error("getCategory - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("getCategory - end id={}", id);
        }
    }

    @Override
    public Category createCategory(Category category) {
        logger.info("createCategory - start name={}", category != null ? category.getName() : null);
        try {
            String normalizedName = normalizeCategoryName(category != null ? category.getName() : null);
            if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
                throw new IllegalArgumentException("Category name already exists.");
            }

            Category entity = new Category();
            entity.setName(normalizedName);
            Category saved = categoryRepository.save(entity);
            logger.info("createCategory - saved id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("createCategory - error", e);
            throw e;
        } finally {
            logger.debug("createCategory - end");
        }
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        logger.info("updateCategory - start id={} name={}", id, category != null ? category.getName() : null);
        try {
            Category existing = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));

            String normalizedName = normalizeCategoryName(category != null ? category.getName() : null);
            categoryRepository.findByNameIgnoreCase(normalizedName)
                    .filter(found -> !found.getId().equals(id))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Category name already exists.");
                    });

            existing.setName(normalizedName);
            Category updated = categoryRepository.save(existing);
            logger.info("updateCategory - saved id={}", id);
            return updated;
        } catch (Exception e) {
            logger.error("updateCategory - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("updateCategory - end id={}", id);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        logger.info("deleteCategory - start id={}", id);
        try {
            long productCount = productRepository.countByCategory_Id(id);
            if (productCount > 0) {
                throw new IllegalStateException("Cannot delete category with existing products");
            }
            categoryRepository.deleteById(id);
            logger.info("deleteCategory - deleted id={}", id);
        } catch (Exception e) {
            logger.error("deleteCategory - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("deleteCategory - end id={}", id);
        }
    }

    private String normalizeCategoryName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        return value.trim();
    }
}

