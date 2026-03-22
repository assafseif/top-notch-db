package com.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.ProductRequest;
import com.project.entity.Category;
import com.project.entity.Product;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public List<Product> getAllProducts() {
        logger.info("GET /api/products - start");
        try {
            List<Product> all = productRepository.findAll();
            logger.info("GET /api/products - found {} products", all.size());
            return all;
        } catch (Exception e) {
            logger.error("GET /api/products - error", e);
            throw e;
        } finally {
            logger.debug("GET /api/products - end");
        }
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        logger.info("GET /api/products/{} - start", id);
        try {
            Product p = productRepository.findById(id).orElse(null);
            logger.info("GET /api/products/{} - result={}", id, (p != null));
            return p;
        } catch (Exception e) {
            logger.error("GET /api/products/{} - error", id, e);
            throw e;
        } finally {
            logger.debug("GET /api/products/{} - end", id);
        }
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductRequest req) {
        logger.info("POST /api/products - start - name={}", req != null ? req.name : null);
        try {
            Product product = new Product();
            product.setName(req.name);
            product.setPrice(req.price != null ? req.price : 0.0);
            product.setOriginalPrice(req.originalPrice);
            product.setDescription(req.description);
            product.setRating(req.rating != null ? req.rating : 0.0);
            product.setReviews(req.reviews != null ? req.reviews : 0);
            product.setSizes(req.sizes);
            product.setColors(req.colors);
            product.setTags(req.tags);
            product.setNew(req.isNew != null ? req.isNew : false);
            product.setLimited(req.isLimited != null ? req.isLimited : false);
            product.setBestseller(req.isBestseller != null ? req.isBestseller : false);

            // normalize category input: accept req.category (string or object), categoryName or categoryId
            String incomingCategoryName = req.categoryName;
            Long incomingCategoryId = req.categoryId;
            if (req.category != null) {
                if (req.category instanceof String) {
                    incomingCategoryName = (String) req.category;
                } else {
                    // attempt to map to a Map and extract id/name
                    try {
                        Map map = objectMapper.convertValue(req.category, Map.class);
                        if (map.containsKey("id")) {
                            Object idv = map.get("id");
                            if (idv instanceof Number) incomingCategoryId = ((Number) idv).longValue();
                            else if (idv instanceof String) incomingCategoryId = Long.parseLong((String) idv);
                        }
                        if (map.containsKey("name") && incomingCategoryName == null) incomingCategoryName = String.valueOf(map.get("name"));
                    } catch (Exception ex) {
                        logger.debug("Could not parse req.category: {}", ex.getMessage());
                    }
                }
            }

            // resolve category by id or name
            Category category = null;
            if (incomingCategoryId != null) {
                category = categoryRepository.findById(incomingCategoryId).orElse(null);
            }
            if (category == null && incomingCategoryName != null && !incomingCategoryName.isBlank()) {
                category = categoryRepository.findByName(incomingCategoryName).orElse(null);
                if (category == null) {
                    Category c = new Category();
                    c.setName(incomingCategoryName);
                    category = categoryRepository.save(c);
                    logger.info("Created new category id={} name={}", category.getId(), category.getName());
                }
            }
            product.setCategory(category);

            // If client provided base64 images convert all images to blobs
            if (req.imagesBase64 != null && !req.imagesBase64.isEmpty()) {
                List<byte[]> blobs = new ArrayList<>();
                for (String base64 : req.imagesBase64) {
                    if (base64 == null || base64.isBlank()) continue;
                    try {
                        byte[] decoded = Base64.getDecoder().decode(base64.replaceFirst("^data:.*;base64,", ""));
                        blobs.add(decoded);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("Failed to decode one base64 image - skipping", ex);
                    }
                }
                if (!blobs.isEmpty()) {
                    product.setImagesBlobs(blobs);
                    // set primary imageBlob to the first item for compatibility
                    product.setImageBlob(blobs.get(0));
                    logger.debug("Converted {} base64 images to blobs, first size={} bytes", blobs.size(), blobs.get(0).length);
                }
            }

            Product saved = productRepository.save(product);
            logger.info("POST /api/products - saved id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("POST /api/products - error", e);
            throw e;
        } finally {
            logger.debug("POST /api/products - end");
        }
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody ProductRequest req) {
        logger.info("PUT /api/products/{} - start - name={}", id, req != null ? req.name : null);
        try {
            Product product = productRepository.findById(id).orElse(new Product());
            product.setId(id);
            product.setName(req.name != null ? req.name : product.getName());
            product.setPrice(req.price != null ? req.price : product.getPrice());
            product.setOriginalPrice(req.originalPrice != null ? req.originalPrice : product.getOriginalPrice());
            product.setDescription(req.description != null ? req.description : product.getDescription());
            product.setRating(req.rating != null ? req.rating : product.getRating());
            product.setReviews(req.reviews != null ? req.reviews : product.getReviews());
            product.setSizes(req.sizes != null ? req.sizes : product.getSizes());
            product.setColors(req.colors != null ? req.colors : product.getColors());
            product.setTags(req.tags != null ? req.tags : product.getTags());
            product.setNew(req.isNew != null ? req.isNew : product.isNew());
            product.setLimited(req.isLimited != null ? req.isLimited : product.isLimited());
            product.setBestseller(req.isBestseller != null ? req.isBestseller : product.isBestseller());

            // normalize category input: accept req.category (string or object), categoryName or categoryId
            String incomingCategoryName = req.categoryName;
            Long incomingCategoryId = req.categoryId;
            if (req.category != null) {
                if (req.category instanceof String) {
                    incomingCategoryName = (String) req.category;
                } else {
                    // attempt to map to a Map and extract id/name
                    try {
                        Map map = objectMapper.convertValue(req.category, Map.class);
                        if (map.containsKey("id")) {
                            Object idv = map.get("id");
                            if (idv instanceof Number) incomingCategoryId = ((Number) idv).longValue();
                            else if (idv instanceof String) incomingCategoryId = Long.parseLong((String) idv);
                        }
                        if (map.containsKey("name") && incomingCategoryName == null) incomingCategoryName = String.valueOf(map.get("name"));
                    } catch (Exception ex) {
                        logger.debug("Could not parse req.category: {}", ex.getMessage());
                    }
                }
            }

            // resolve category
            Category category = null;
            if (incomingCategoryId != null) {
                category = categoryRepository.findById(incomingCategoryId).orElse(null);
            }
            if (category == null && incomingCategoryName != null && !incomingCategoryName.isBlank()) {
                category = categoryRepository.findByName(incomingCategoryName).orElse(null);
                if (category == null) {
                    Category c = new Category();
                    c.setName(incomingCategoryName);
                    category = categoryRepository.save(c);
                    logger.info("Created new category id={} name={}", category.getId(), category.getName());
                }
            }
            product.setCategory(category);

            // images convert - if provided, replace existing images
            if (req.imagesBase64 != null) {
                List<byte[]> blobs = new ArrayList<>();
                for (String base64 : req.imagesBase64) {
                    if (base64 == null || base64.isBlank()) continue;
                    try {
                        byte[] decoded = Base64.getDecoder().decode(base64.replaceFirst("^data:.*;base64,", ""));
                        blobs.add(decoded);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("Failed to decode one base64 image - skipping", ex);
                    }
                }
                if (!blobs.isEmpty()) {
                    product.setImagesBlobs(blobs);
                    product.setImageBlob(blobs.get(0));
                    logger.debug("Updated product {} with {} image blobs", id, blobs.size());
                }
            }

            Product updated = productRepository.save(product);
            logger.info("PUT /api/products/{} - saved", id);
            return updated;
        } catch (Exception e) {
            logger.error("PUT /api/products/{} - error", id, e);
            throw e;
        } finally {
            logger.debug("PUT /api/products/{} - end", id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /api/products/{} - start", id);
        try {
            productRepository.deleteById(id);
            logger.info("DELETE /api/products/{} - deleted", id);
        } catch (Exception e) {
            logger.error("DELETE /api/products/{} - error", id, e);
            throw e;
        } finally {
            logger.debug("DELETE /api/products/{} - end", id);
        }
    }
}
