package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.BrandRenameRequest;
import com.project.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {
    private static final Logger logger = LoggerFactory.getLogger(BrandController.class);

    @Autowired
    private BrandService brandService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('products.create', 'products.edit', 'brands.create', 'brands.edit')")
    public List<String> getAllBrands() {
        logger.info("GET /api/brands - start");
        try {
            return brandService.getAllBrands();
        } finally {
            logger.debug("GET /api/brands - end");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('brands.create')")
    public ApiResponse<String> createBrand(@RequestBody BrandRenameRequest request) {
        logger.info("POST /api/brands - start name={}", request != null ? request.getNewName() : null);
        try {
            return ApiResponse.of(
                    "Brand created successfully.",
                    brandService.createBrand(request != null ? request.getNewName() : null)
            );
        } finally {
            logger.debug("POST /api/brands - end");
        }
    }

    @PutMapping("/rename")
    @PreAuthorize("hasAuthority('brands.edit')")
    public ApiResponse<Void> renameBrand(@RequestBody BrandRenameRequest request) {
        logger.info("PUT /api/brands/rename - start currentName={} newName={}", request != null ? request.getCurrentName() : null, request != null ? request.getNewName() : null);
        try {
            long updatedCount = brandService.renameBrand(
                    request != null ? request.getCurrentName() : null,
                    request != null ? request.getNewName() : null
            );
            return ApiResponse.of("Brand updated successfully for " + updatedCount + " product(s).");
        } finally {
            logger.debug("PUT /api/brands/rename - end");
        }
    }
}