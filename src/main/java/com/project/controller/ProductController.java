package com.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.ApiResponse;
import com.project.dto.ProductDto;
import com.project.dto.ProductRequest;
import com.project.entity.Category;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import com.project.service.ProductService;
import com.project.service.StoreConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    private StoreConfigurationService storeConfigurationService;


    // Unpaged (legacy)
    @GetMapping
    @PreAuthorize("hasAuthority('products.view')")
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
    @PreAuthorize("hasAuthority('products.view')")
    public Page<ProductDto> getAllProductsPaged(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(defaultValue = "newest") String sortBy,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(required = false) Integer size) {
        int resolvedSize = storeConfigurationService.resolvePageSize(size);
        logger.info("GET /api/products/paged - start categoryId={} search={} sortBy={} page={} size={}", categoryId, search, sortBy, page, resolvedSize);
        try {
            return productService.getAdminProducts(categoryId, search, sortBy, page, resolvedSize);
        } finally {
            logger.debug("GET /api/products/paged - end");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('products.view')")
    public ProductDto getProduct(@PathVariable Long id) {
        logger.info("GET /api/products/{} - start", id);
        try {
            return productService.getProductWithImages(id);
        } finally {
            logger.debug("GET /api/products/{} - end", id);
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('products.create')")
    public ApiResponse<ProductDto> createProduct(@RequestBody ProductRequest req) {
        logger.info("POST /api/products - start - name={}", req != null ? req.getName() : null);
        try {
            Long productId = productService.createProduct(req).getId();
            return ApiResponse.of("Product created successfully.", productService.getProductWithImages(productId));
        } finally {
            logger.debug("POST /api/products - end");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('products.edit')")
    public ApiResponse<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductRequest req) {
        logger.info("PUT /api/products/{} - start - name={}", id, req != null ? req.getName() : null);
        try {
            productService.updateProduct(id, req);
            return ApiResponse.of("Product updated successfully.", productService.getProductWithImages(id));
        } finally {
            logger.debug("PUT /api/products/{} - end", id);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('products.delete')")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /api/products/{} - start", id);
        try {
            productService.deleteProduct(id);
            return ApiResponse.of("Product deleted successfully.");
        } finally {
            logger.debug("DELETE /api/products/{} - end", id);
        }
    }

    // Unpaged (legacy)
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('products.view')")
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
    @PreAuthorize("hasAuthority('products.view')")
    public Page<ProductDto> getProductsByCategoryPaged(@PathVariable Long categoryId,
                                                      @RequestParam(defaultValue = "newest") String sortBy,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(required = false) Integer size) {
        int resolvedSize = storeConfigurationService.resolvePageSize(size);
        logger.info("GET /api/products/category/{}/paged - start sortBy={} page={} size={}", categoryId, sortBy, page, resolvedSize);
        try {
            return productService.getAdminProducts(categoryId, null, sortBy, page, resolvedSize);
        } finally {
            logger.debug("GET /api/products/category/{}/paged - end", categoryId);
        }
    }
}
