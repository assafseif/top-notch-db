package com.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProductsWithImagesPaged(int page, int size) {
        logger.info("getAllProductsWithImagesPaged - start page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductDto> dtos = new ArrayList<>();
        for (Product p : productPage.getContent()) {
            dtos.add(mapToDto(p));
        }
        Page<ProductDto> dtoPage = new PageImpl<>(dtos, pageable, productPage.getTotalElements());
        logger.info("getAllProductsWithImagesPaged - returning {} items", dtos.size());
        return dtoPage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategoryIdPaged(Long categoryId, int page, int size) {
        logger.info("getProductsByCategoryIdPaged - start categoryId={} page={} size={}", categoryId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAllByCategory_Id(categoryId, pageable);
        List<ProductDto> dtos = new ArrayList<>();
        for (Product p : productPage.getContent()) {
            dtos.add(mapToDto(p));
        }
        Page<ProductDto> dtoPage = new PageImpl<>(dtos, pageable, productPage.getTotalElements());
        logger.info("getProductsByCategoryIdPaged - returning {} items for categoryId={}", dtos.size(), categoryId);
        return dtoPage;
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
                for (String base64 : req.getImagesBase64()) {
                    if (base64 == null || base64.isBlank()) continue;
                    try {
                        byte[] decoded = Base64.getDecoder().decode(base64.replaceFirst("^data:.*;base64,", ""));
                        Image img = new Image();
                        img.setData(decoded);
                        imgs.add(img);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("Failed to decode one base64 image - skipping", ex);
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
            Product product = productRepository.findById(id).orElse(new Product());
            product.setId(id);
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
                for (String base64 : req.getImagesBase64()) {
                    if (base64 == null || base64.isBlank()) continue;
                    try {
                        byte[] decoded = Base64.getDecoder().decode(base64.replaceFirst("^data:.*;base64,", ""));
                        Image img = new Image();
                        img.setData(decoded);
                        imgs.add(img);
                    } catch (IllegalArgumentException ex) {
                        logger.warn("Failed to decode one base64 image - skipping", ex);
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
                if (p.getCategory() != null) {
                    dto.setCategoryId(p.getCategory().getId());
                    dto.setCategoryName(p.getCategory().getName());
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
            List<Product> products = productRepository.findAll();
            List<ProductDto> dtos = new ArrayList<>();
            for (Product p : products) {
                dtos.add(mapToDto(p));
            }
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
            List<Product> products = productRepository.findAllByCategory_Id(categoryId);
            List<ProductDto> dtos = new ArrayList<>();
            for (Product p : products) {
                dtos.add(mapToDto(p));
            }
            logger.info("getProductsByCategoryId - returning {} items for categoryId={}", dtos.size(), categoryId);
            return dtos;
        } catch (Exception e) {
            logger.error("getProductsByCategoryId - error categoryId={}", categoryId, e);
            throw e;
        } finally {
            logger.debug("getProductsByCategoryId - end categoryId={}", categoryId);
        }
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

        // primary image
        if (p.getImageBlob() != null && p.getImageBlob().length > 0) {
            try {
                String base64 = Base64.getEncoder().encodeToString(p.getImageBlob());
                dto.setImageBase64("data:image/*;base64," + base64);
            } catch (Exception e) {
                logger.warn("mapToDto - failed to encode primary image for product {}", p.getId(), e);
            }
        }

        // additional images from Image entities
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            List<String> images = new ArrayList<>();
            for (Image img : p.getImages()) {
                if (img == null || img.getData() == null || img.getData().length == 0) continue;
                try {
                    images.add("data:image/*;base64," + Base64.getEncoder().encodeToString(img.getData()));
                } catch (Exception e) {
                    logger.warn("mapToDto - failed to encode one image for product {}", p.getId(), e);
                }
            }
            if (!images.isEmpty()) dto.setImagesBase64(images);
        }

        if (p.getCategory() != null) {
            dto.setCategory(p.getCategory());
        }
        return dto;
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
