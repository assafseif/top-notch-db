package com.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.CategorySummaryDto;
import com.project.dto.ProductCategoryDto;
import com.project.dto.ProductDto;
import com.project.dto.ProductRequest;
import com.project.entity.Category;
import com.project.entity.Product;
import com.project.entity.Image;
import com.project.entity.Tag;
import com.project.entity.Size;
import com.project.entity.Color;
import com.project.repository.CategoryRepository;
import com.project.repository.ProductRepository;
import com.project.repository.ImageRepository;
import com.project.repository.TagRepository;
import com.project.repository.SizeRepository;
import com.project.repository.ColorRepository;
import com.project.service.ProductService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProductsWithImagesPaged(int page, int size) {
        return getStoreProducts(null, "newest", page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategoryIdPaged(Long categoryId, int page, int size) {
        return getStoreProducts(categoryId, "newest", page, size);
    }

    @Override
    public List<Product> getAllProducts() {
        logger.info("getAllProducts - start");
        try {
            List<Product> all = productRepository.findAll();
            logger.info("getAllProducts - found {} products", all.size());
            return all;
        } catch (Exception e) {
            logger.error("getAllProducts - error", e);
            throw e;
        } finally {
            logger.debug("getAllProducts - end");
        }
    }

    @Override
    public Product getProduct(Long id) {
        logger.info("getProduct - start id={}", id);
        try {
            Product p = productRepository.findById(id).orElse(null);
            logger.info("getProduct - result={}", (p != null));
            return p;
        } catch (Exception e) {
            logger.error("getProduct - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("getProduct - end id={}", id);
        }
    }

    @Override
    public Product createProduct(ProductRequest req) {
        logger.info("createProduct - start name={}", req != null ? req.getName() : null);
        try {
            validateProductRequest(req, true);
            Product product = new Product();
            product.setName(req.getName());
            product.setPrice(req.getPrice() != null ? req.getPrice() : 0.0);
            product.setOriginalPrice(req.getOriginalPrice());
            product.setDescription(req.getDescription());
            product.setRating(req.getRating() != null ? req.getRating() : 0.0);
            product.setReviews(req.getReviews() != null ? req.getReviews() : 0);
            product.setQuantity(req.getQuantity() != null ? req.getQuantity() : 0);

            // map sizes/colors/tags from strings to entities
            product.setSizes(resolveSizes(req.getSizes()));
            product.setColors(resolveColors(req.getColors()));
            product.setTags(resolveTags(req.getTags()));

            product.setNew(req.getIsNew() != null ? req.getIsNew() : false);
            product.setLimited(req.getIsLimited() != null ? req.getIsLimited() : false);
            product.setBestseller(req.getIsBestseller() != null ? req.getIsBestseller() : false);

            // normalize category input: accept req.category (string or object), categoryName or categoryId
            String incomingCategoryName = req.getCategoryName();
            Long incomingCategoryId = req.getCategoryId();

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

            // If client provided base64 images convert all images to Image entities
            if (req.getImagesBase64() != null && !req.getImagesBase64().isEmpty()) {
                List<Image> imgs = new ArrayList<>();
                for (String imagePayload : req.getImagesBase64()) {
                    Image img = decodeImagePayload(imagePayload);
                    if (img != null) {
                        imgs.add(img);
                    }
                }
                if (!imgs.isEmpty()) {
                    // save images first so they get ids
                    List<Image> savedImages = imageRepository.saveAll(imgs);
                    product.setImages(savedImages);
                    // set primary imageBlob to the first item's data for compatibility
                    if (savedImages.get(0).getData() != null) {
                        product.setImageBlob(savedImages.get(0).getData());
                        logger.debug("Converted {} base64 images to Image entities, first size={} bytes", savedImages.size(), savedImages.get(0).getData().length);
                    }
                }
            }

            Product saved = productRepository.save(product);
            logger.info("createProduct - saved id={}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("createProduct - error", e);
            throw e;
        } finally {
            logger.debug("createProduct - end");
        }
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequest req) {
        logger.info("updateProduct - start id={} name={}", id, req != null ? req.getName() : null);
        try {
            validateProductRequest(req, false);
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Product not found."));
            product.setName(req.getName() != null ? req.getName() : product.getName());
            product.setPrice(req.getPrice() != null ? req.getPrice() : product.getPrice());
            product.setOriginalPrice(req.getOriginalPrice() != null ? req.getOriginalPrice() : product.getOriginalPrice());
            product.setDescription(req.getDescription() != null ? req.getDescription() : product.getDescription());
            product.setRating(req.getRating() != null ? req.getRating() : product.getRating());
            product.setReviews(req.getReviews() != null ? req.getReviews() : product.getReviews());
            product.setQuantity(req.getQuantity() != null ? req.getQuantity() : product.getQuantity());

            // update sizes/colors/tags if provided
            if (req.getSizes() != null) product.setSizes(resolveSizes(req.getSizes()));
            if (req.getColors() != null) product.setColors(resolveColors(req.getColors()));
            if (req.getTags() != null) product.setTags(resolveTags(req.getTags()));

            product.setNew(req.getIsNew() != null ? req.getIsNew() : product.isNew());
            product.setLimited(req.getIsLimited() != null ? req.getIsLimited() : product.isLimited());
            product.setBestseller(req.getIsBestseller() != null ? req.getIsBestseller() : product.isBestseller());

            // normalize category input: accept req.category (string or object), categoryName or categoryId
            String incomingCategoryName = req.getCategoryName();
            Long incomingCategoryId = req.getCategoryId();


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
            if (req.getImagesBase64() != null) {
                List<Image> imgs = new ArrayList<>();
                for (String imagePayload : req.getImagesBase64()) {
                    Image img = decodeImagePayload(imagePayload);
                    if (img != null) {
                        imgs.add(img);
                    }
                }
                if (!imgs.isEmpty()) {
                    List<Image> savedImages = imageRepository.saveAll(imgs);
                    product.setImages(savedImages);
                    product.setImageBlob(savedImages.get(0).getData());
                    logger.debug("Updated product {} with {} Image entities", id, savedImages.size());
                }
            }

            Product updated = productRepository.save(product);
            logger.info("updateProduct - saved id={}", id);
            return updated;
        } catch (Exception e) {
            logger.error("updateProduct - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("updateProduct - end id={}", id);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        logger.info("deleteProduct - start id={}", id);
        try {
            if (!productRepository.existsById(id)) {
                throw new NoSuchElementException("Product not found.");
            }
            productRepository.deleteById(id);
            logger.info("deleteProduct - deleted id={}", id);
        } catch (Exception e) {
            logger.error("deleteProduct - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("deleteProduct - end id={}", id);
        }
    }

    @Override
    public List<ProductCategoryDto> getAllProductCategories() {
        logger.info("getAllProductCategories - start");
        try {
            List<Product> products = productRepository.findAll();
            List<ProductCategoryDto> dtos = new ArrayList<>();
            for (Product p : products) {
                ProductCategoryDto dto = new ProductCategoryDto();
                dto.setId(p.getId());
                dto.setName(p.getName());
                dto.setPrice(p.getPrice());
                CategorySummaryDto category = mapCategorySummary(p);
                if (category != null) {
                    dto.setCategoryId(category.getId());
                    dto.setCategoryName(category.getName());
                }
                dtos.add(dto);
            }
            logger.info("getAllProductCategories - returning {} items", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("getAllProductCategories - error", e);
            throw e;
        } finally {
            logger.debug("getAllProductCategories - end");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProductsWithImages() {
        logger.info("getAllProductsWithImages - start");
        try {
            List<ProductDto> dtos = getStoreProducts(null, "newest", 0, Integer.MAX_VALUE).getContent();
            logger.info("getAllProductsWithImages - returning {} items", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("getAllProductsWithImages - error", e);
            throw e;
        } finally {
            logger.debug("getAllProductsWithImages - end");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductWithImages(Long id) {
        logger.info("getProductWithImages - start id={}", id);
        try {
            Product p = productRepository.findById(id).orElse(null);
            if (p == null) {
                logger.info("getProductWithImages - not found id={}", id);
                return null;
            }
            ProductDto dto = mapToDto(p);
            logger.info("getProductWithImages - found id={}", id);
            return dto;
        } catch (Exception e) {
            logger.error("getProductWithImages - error id={}", id, e);
            throw e;
        } finally {
            logger.debug("getProductWithImages - end id={}", id);
        }
    }

    // new: get products by category id and map to DTOs
    @Override
    @Transactional(readOnly = true)
    public List<com.project.dto.ProductDto> getProductsByCategoryId(Long categoryId) {
        logger.info("getProductsByCategoryId - start categoryId={}", categoryId);
        try {
            List<ProductDto> dtos = getStoreProducts(categoryId, "newest", 0, Integer.MAX_VALUE).getContent();
            logger.info("getProductsByCategoryId - returning {} items for categoryId={}", dtos.size(), categoryId);
            return dtos;
        } catch (Exception e) {
            logger.error("getProductsByCategoryId - error categoryId={}", categoryId, e);
            throw e;
        } finally {
            logger.debug("getProductsByCategoryId - end categoryId={}", categoryId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getStoreProducts(Long categoryId, String sortBy, int page, int size) {
        logger.info("getStoreProducts - start categoryId={} sortBy={} page={} size={}", categoryId, sortBy, page, size);
        return queryProducts(categoryId, null, sortBy, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAdminProducts(Long categoryId, String search, String sortBy, int page, int size) {
        logger.info("getAdminProducts - start categoryId={} search={} sortBy={} page={} size={}", categoryId, search, sortBy, page, size);
        return queryProducts(categoryId, search, sortBy, page, size);
    }

    private Page<ProductDto> queryProducts(Long categoryId, String search, String sortBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
        Specification<Product> specification = Specification.where(hasCategory(categoryId)).and(matchesSearch(search));
        Page<Product> productPage = productRepository.findAll(specification, pageable);
        List<ProductDto> dtos = new ArrayList<>();
        for (Product product : productPage.getContent()) {
            dtos.add(mapToDto(product));
        }
        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }

    private Specification<Product> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.join("category").get("id"), categoryId);
        };
    }

    private Specification<Product> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("category").get("name")), pattern)
            );
        };
    }

    private Sort resolveSort(String sortBy) {
        String normalizedSort = sortBy == null ? "newest" : sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedSort) {
            case "popular" -> Sort.by(Sort.Order.desc("reviews"), Sort.Order.desc("rating"), Sort.Order.desc("id"));
            case "rating" -> Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("reviews"), Sort.Order.desc("id"));
            case "price-low" -> Sort.by(Sort.Order.asc("price"), Sort.Order.desc("id"));
            case "price-high" -> Sort.by(Sort.Order.desc("price"), Sort.Order.desc("id"));
            default -> Sort.by(Sort.Order.desc("id"));
        };
    }

    private ProductDto mapToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setPrice(p.getPrice());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setDescription(p.getDescription());
        dto.setRating(p.getRating());
        dto.setReviews(p.getReviews());

        // sizes/colors/tags -> strings
        if (p.getSizes() != null) {
            List<String> sizes = new ArrayList<>();
            for (Size s : p.getSizes()) {
                if (s != null) sizes.add(s.getValue());
            }
            dto.setSizes(sizes);
        }
        if (p.getColors() != null) {
            List<String> colors = new ArrayList<>();
            for (Color c : p.getColors()) {
                if (c != null) colors.add(c.getValue());
            }
            dto.setColors(colors);
        }
        if (p.getTags() != null) {
            List<String> tags = new ArrayList<>();
            for (Tag t : p.getTags()) {
                if (t != null) tags.add(t.getName());
            }
            dto.setTags(tags);
        }

        dto.setIsNew(p.isNew());
        dto.setIsLimited(p.isLimited());
        dto.setIsBestseller(p.isBestseller());
        dto.setQuantity(p.getQuantity());

        // Prefer cacheable image URLs over embedding image bytes in the JSON payload.
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (Image img : p.getImages()) {
                if (img == null || img.getId() == null) {
                    continue;
                }
                imageUrls.add("/api/images/" + img.getId());
            }
            if (!imageUrls.isEmpty()) {
                dto.setImageUrls(imageUrls);
                dto.setImageUrl(imageUrls.get(0));
            }
        }

        if (dto.getImageUrl() == null && p.getImageBlob() != null && p.getImageBlob().length > 0) {
            String primaryImageUrl = "/api/store/products/" + p.getId() + "/primary-image";
            dto.setImageUrl(primaryImageUrl);
            dto.setImageUrls(List.of(primaryImageUrl));
        }

        dto.setCategory(mapCategorySummary(p));
        return dto;
    }

    private Image decodeImagePayload(String imagePayload) {
        if (imagePayload == null || imagePayload.isBlank()) {
            return null;
        }

        try {
            Image image = new Image();
            String cleanBase64 = imagePayload;
            if (imagePayload.startsWith("data:")) {
                String[] parts = imagePayload.split(",", 2);
                if (parts.length == 2) {
                    cleanBase64 = parts[1];
                    image.setContentType(parts[0].replace("data:", "").replace(";base64", ""));
                }
            }
            image.setData(Base64.getDecoder().decode(cleanBase64));
            return image;
        } catch (IllegalArgumentException ex) {
            logger.warn("Failed to decode one base64 image - skipping", ex);
            return null;
        }
    }

    private CategorySummaryDto mapCategorySummary(Product product) {
        if (product == null || product.getCategory() == null) {
            return null;
        }

        Object categoryIdentifier = entityManagerFactory
                .getPersistenceUnitUtil()
                .getIdentifier(product.getCategory());

        Long categoryId = categoryIdentifier instanceof Long ? (Long) categoryIdentifier : null;
        try {
            return new CategorySummaryDto(categoryId, product.getCategory().getName());
        } catch (EntityNotFoundException exception) {
            logger.warn("Product {} references missing category {}", product.getId(), categoryId);
            return new CategorySummaryDto(categoryId, null);
        }
    }

    private void validateProductRequest(ProductRequest req, boolean creating) {
        if (req == null) {
            throw new IllegalArgumentException("Product details are required.");
        }
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (req.getPrice() == null || req.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero.");
        }
        if (req.getQuantity() != null && req.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative.");
        }
        if (req.getCategoryId() == null && (req.getCategoryName() == null || req.getCategoryName().trim().isEmpty())) {
            throw new IllegalArgumentException("Product category is required.");
        }
        if (creating && (req.getImagesBase64() == null || req.getImagesBase64().isEmpty())) {
            throw new IllegalArgumentException("At least one product image is required.");
        }
    }

    // helpers to resolve or create tag/size/color entities
    private List<Tag> resolveTags(List<String> tagNames) {
        List<Tag> out = new ArrayList<>();
        if (tagNames == null) return out;
        for (String n : tagNames) {
            if (n == null || n.isBlank()) continue;
            String trimmed = n.trim();
            Tag t = tagRepository.findByName(trimmed).orElseGet(() -> tagRepository.save(new Tag(trimmed)));
            out.add(t);
        }
        return out;
    }

    private List<Size> resolveSizes(List<String> sizeValues) {
        List<Size> out = new ArrayList<>();
        if (sizeValues == null) return out;
        for (String v : sizeValues) {
            if (v == null || v.isBlank()) continue;
            String trimmed = v.trim();
            Size s = sizeRepository.findByValue(trimmed).orElseGet(() -> sizeRepository.save(new Size(trimmed)));
            out.add(s);
        }
        return out;
    }

    private List<Color> resolveColors(List<String> colorValues) {
        List<Color> out = new ArrayList<>();
        if (colorValues == null) return out;
        for (String v : colorValues) {
            if (v == null || v.isBlank()) continue;
            String trimmed = v.trim();
            Color c = colorRepository.findByValue(trimmed).orElseGet(() -> colorRepository.save(new Color(trimmed)));
            out.add(c);
        }
        return out;
    }
}
