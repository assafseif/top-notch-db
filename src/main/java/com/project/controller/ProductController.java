package com.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.ProductDto;
import com.project.dto.ProductRequest;
import com.project.entity.Category;
import com.project.entity.Product;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import com.project.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;


    // Unpaged (legacy)
    @GetMapping
    public List<ProductDto> getAllProducts() {
        logger.info("GET /api/products - start");
        try {
            return productService.getAllProductsWithImages();
        } finally {
            logger.debug("GET /api/products - end");
        }
    }

    // Paged
    @GetMapping("/paged")
    public Page<ProductDto> getAllProductsPaged(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/products/paged - start page={} size={}", page, size);
        try {
            return productService.getAllProductsWithImagesPaged(page, size);
        } finally {
            logger.debug("GET /api/products/paged - end");
        }
    }

    @GetMapping("/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        logger.info("GET /api/products/{} - start", id);
        try {
            return productService.getProductWithImages(id);
        } finally {
            logger.debug("GET /api/products/{} - end", id);
        }
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductRequest req) {
        logger.info("POST /api/products - start - name={}", req != null ? req.getName() : null);
        try {
            return productService.createProduct(req);
        } finally {
            logger.debug("POST /api/products - end");
        }
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody ProductRequest req) {
        logger.info("PUT /api/products/{} - start - name={}", id, req != null ? req.getName() : null);
        try {
            return productService.updateProduct(id, req);
        } finally {
            logger.debug("PUT /api/products/{} - end", id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /api/products/{} - start", id);
        try {
            productService.deleteProduct(id);
        } finally {
            logger.debug("DELETE /api/products/{} - end", id);
        }
    }

    // Unpaged (legacy)
    @GetMapping("/category/{categoryId}")
    public List<ProductDto> getProductsByCategory(@PathVariable Long categoryId) {
        logger.info("GET /api/products/category/{} - start", categoryId);
        try {
            return productService.getProductsByCategoryId(categoryId);
        } finally {
            logger.debug("GET /api/products/category/{} - end", categoryId);
        }
    }

    // Paged
    @GetMapping("/category/{categoryId}/paged")
    public Page<ProductDto> getProductsByCategoryPaged(@PathVariable Long categoryId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/products/category/{}/paged - start page={} size={}", categoryId, page, size);
        try {
            return productService.getProductsByCategoryIdPaged(categoryId, page, size);
        } finally {
            logger.debug("GET /api/products/category/{}/paged - end", categoryId);
        }
    }
}
