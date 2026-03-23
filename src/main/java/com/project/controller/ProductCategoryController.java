package com.project.controller;

import com.project.dto.ProductCategoryDto;
import com.project.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productscategories")
public class ProductCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductCategoryDto>> getAll() {
        logger.info("GET /api/productscategories - start");
        try {
            List<ProductCategoryDto> dtos = productService.getAllProductCategories();
            logger.info("GET /api/productscategories - returning {} items", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("GET /api/productscategories - error", e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
