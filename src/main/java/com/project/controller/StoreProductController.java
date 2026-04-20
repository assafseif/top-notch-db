package com.project.controller;

import com.project.dto.ProductDto;
import com.project.dto.StoreProductFiltersDto;
import com.project.entity.Product;
import com.project.service.ProductService;
import com.project.service.StoreConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/store/products")
public class StoreProductController {
    private static final Logger logger = LoggerFactory.getLogger(StoreProductController.class);

    private final ProductService productService;
    private final StoreConfigurationService storeConfigurationService;

    public StoreProductController(ProductService productService, StoreConfigurationService storeConfigurationService) {
        this.productService = productService;
        this.storeConfigurationService = storeConfigurationService;
    }

    @GetMapping
    public List<ProductDto> getAllStoreProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subcategory,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(name = "gender", required = false) List<String> genders,
            @RequestParam(name = "brand", required = false) List<String> brands,
            @RequestParam(name = "size", required = false) List<String> sizes,
            @RequestParam(name = "color", required = false) List<String> colors,
            @RequestParam(name = "feature", required = false) List<String> features
    ) {
        logger.info("GET /api/store/products - start categoryId={} search={} subcategory={} sortBy={}", categoryId, search, subcategory, sortBy);
        try {
            return productService.getStoreProducts(categoryId, search, subcategory, sortBy, minPrice, maxPrice, genders, brands, sizes, colors, features, 0, Integer.MAX_VALUE).getContent();
        } finally {
            logger.debug("GET /api/store/products - end");
        }
    }

    @GetMapping("/paged")
    public Page<ProductDto> getStoreProductsPaged(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subcategory,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(name = "gender", required = false) List<String> genders,
            @RequestParam(name = "brand", required = false) List<String> brands,
            @RequestParam(name = "size", required = false) List<String> sizes,
            @RequestParam(name = "color", required = false) List<String> colors,
            @RequestParam(name = "feature", required = false) List<String> features,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "pageSize", required = false) Integer pageSize
    ) {
        int resolvedPageSize = storeConfigurationService.resolvePageSize(pageSize);
        logger.info("GET /api/store/products/paged - start categoryId={} search={} subcategory={} sortBy={} page={} size={}", categoryId, search, subcategory, sortBy, page, resolvedPageSize);
        try {
            return productService.getStoreProducts(categoryId, search, subcategory, sortBy, minPrice, maxPrice, genders, brands, sizes, colors, features, page, resolvedPageSize);
        } finally {
            logger.debug("GET /api/store/products/paged - end");
        }
    }

    @GetMapping("/filters")
    public StoreProductFiltersDto getStoreProductFilters(@RequestParam(required = false) Long categoryId) {
        logger.info("GET /api/store/products/filters - start categoryId={}", categoryId);
        try {
            return productService.getStoreProductFilters(categoryId);
        } finally {
            logger.debug("GET /api/store/products/filters - end");
        }
    }

    @GetMapping("/{id}")
    public ProductDto getStoreProduct(@PathVariable Long id) {
        logger.info("GET /api/store/products/{} - start", id);
        try {
            return productService.getProductWithImages(id);
        } finally {
            logger.debug("GET /api/store/products/{} - end", id);
        }
    }

    @GetMapping("/{id}/primary-image")
    public ResponseEntity<byte[]> getStoreProductPrimaryImage(@PathVariable Long id, ServletWebRequest request) throws IOException {
        logger.info("GET /api/store/products/{}/primary-image - start", id);
        try {
            Product product = productService.getProduct(id);
            if (product == null || product.getImageBlob() == null || product.getImageBlob().length == 0) {
                return ResponseEntity.notFound().build();
            }

            return buildImageResponse(
                    product.getImageBlob(),
                    null,
                    CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic(),
                    request
            );
        } finally {
            logger.debug("GET /api/store/products/{}/primary-image - end", id);
        }
    }

    private ResponseEntity<byte[]> buildImageResponse(
            byte[] data,
            String contentType,
            CacheControl cacheControl,
            ServletWebRequest request
    ) throws IOException {
        String etag = '"' + DigestUtils.md5DigestAsHex(data) + '"';
        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(304)
                    .eTag(etag)
                    .cacheControl(cacheControl)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(resolveMediaType(contentType, data))
                .contentLength(data.length)
                .cacheControl(cacheControl)
                .eTag(etag)
                .body(data);
    }

    private MediaType resolveMediaType(String explicitContentType, byte[] data) throws IOException {
        if (explicitContentType != null && !explicitContentType.isBlank()) {
            return MediaType.parseMediaType(explicitContentType);
        }

        String guessedContentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(data));
        if (guessedContentType != null && !guessedContentType.isBlank()) {
            return MediaType.parseMediaType(guessedContentType);
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}