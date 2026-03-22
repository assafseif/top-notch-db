package com.project.controller;

import com.project.entity.Product;
import com.project.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/productscategories")
public class ProductCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryController.class);

    @Autowired
    private ProductRepository productRepository;

    public static class ProductCategoryDto {
        public Long id;
        public String name;
        public double price;
        public Long categoryId;
        public String categoryName;

        public ProductCategoryDto(Long id, String name, double price, Long categoryId, String categoryName) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductCategoryDto>> getAll() {
        logger.info("GET /api/productscategories - start");
        try {
            List<Product> products = productRepository.findAll();
            List<ProductCategoryDto> dtos = new ArrayList<>();
            for (Product p : products) {
                Long catId = null;
                String catName = null;
                try {
                    if (p.getCategory() != null) {
                        catId = p.getCategory().getId();
                        catName = p.getCategory().getName();
                    }
                } catch (Exception ex) {
                    logger.debug("Could not read category for product id={}: {}", p.getId(), ex.getMessage());
                }
                dtos.add(new ProductCategoryDto(p.getId(), p.getName(), p.getPrice(), catId, catName));
            }
            logger.info("GET /api/productscategories - returning {} items", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("GET /api/productscategories - error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

