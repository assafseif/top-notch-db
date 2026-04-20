package com.project.service.impl;

import com.project.dto.SubcategoryRequest;
import com.project.entity.Category;
import com.project.entity.Subcategory;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import com.project.repository.SubcategoryRepository;
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

    @Autowired
    private SubcategoryRepository subcategoryRepository;

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

    @Override
    public Subcategory createSubcategory(Long categoryId, SubcategoryRequest request) {
        logger.info("createSubcategory - start categoryId={} name={}", categoryId, request != null ? request.getName() : null);
        try {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));

            String normalizedName = normalizeRequiredName(request != null ? request.getName() : null, "Subcategory name is required.");
            if (subcategoryRepository.existsByCategory_IdAndNameIgnoreCase(categoryId, normalizedName)) {
                throw new IllegalArgumentException("Subcategory name already exists for this category.");
            }

            Subcategory subcategory = new Subcategory();
            subcategory.setName(normalizedName);
            subcategory.setCategory(category);
            return subcategoryRepository.save(subcategory);
        } catch (Exception e) {
            logger.error("createSubcategory - error categoryId={}", categoryId, e);
            throw e;
        } finally {
            logger.debug("createSubcategory - end categoryId={}", categoryId);
        }
    }

    @Override
    public Subcategory updateSubcategory(Long subcategoryId, SubcategoryRequest request) {
        logger.info("updateSubcategory - start id={} name={} categoryId={}", subcategoryId, request != null ? request.getName() : null, request != null ? request.getCategoryId() : null);
        try {
            Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Subcategory not found."));

            Long targetCategoryId = request != null && request.getCategoryId() != null
                    ? request.getCategoryId()
                    : (subcategory.getCategory() != null ? subcategory.getCategory().getId() : null);
            if (targetCategoryId == null) {
                throw new IllegalArgumentException("Category is required.");
            }

            Category targetCategory = categoryRepository.findById(targetCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));

            String normalizedName = normalizeRequiredName(request != null ? request.getName() : null, "Subcategory name is required.");
            subcategoryRepository.findByCategory_IdAndNameIgnoreCase(targetCategoryId, normalizedName)
                    .filter(found -> !found.getId().equals(subcategoryId))
                    .ifPresent(found -> {
                        throw new IllegalArgumentException("Subcategory name already exists for this category.");
                    });

            subcategory.setName(normalizedName);
            subcategory.setCategory(targetCategory);
            return subcategoryRepository.save(subcategory);
        } catch (Exception e) {
            logger.error("updateSubcategory - error id={}", subcategoryId, e);
            throw e;
        } finally {
            logger.debug("updateSubcategory - end id={}", subcategoryId);
        }
    }

    @Override
    public void deleteSubcategory(Long subcategoryId) {
        logger.info("deleteSubcategory - start id={}", subcategoryId);
        try {
            if (!subcategoryRepository.existsById(subcategoryId)) {
                throw new IllegalArgumentException("Subcategory not found.");
            }

            long productCount = productRepository.countBySubcategory_Id(subcategoryId);
            if (productCount > 0) {
                throw new IllegalStateException("Cannot delete subcategory with existing products.");
            }

            subcategoryRepository.deleteById(subcategoryId);
        } catch (Exception e) {
            logger.error("deleteSubcategory - error id={}", subcategoryId, e);
            throw e;
        } finally {
            logger.debug("deleteSubcategory - end id={}", subcategoryId);
        }
    }

    private String normalizeCategoryName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }
        return value.trim();
    }

    private String normalizeRequiredName(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}

