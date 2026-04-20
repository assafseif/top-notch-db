package com.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.CategorySummaryDto;
import com.project.dto.ProductCategoryDto;
import com.project.dto.ProductDto;
import com.project.dto.ProductRequest;
import com.project.dto.StoreProductFiltersDto;
import com.project.entity.Brand;
import com.project.entity.Category;
import com.project.entity.Product;
import com.project.entity.Image;
import com.project.entity.Subcategory;
import com.project.entity.Tag;
import com.project.entity.Size;
import com.project.entity.Color;
import com.project.repository.CategoryRepository;
import com.project.repository.BrandRepository;
import com.project.repository.ProductRepository;
import com.project.repository.ImageRepository;
import com.project.repository.TagRepository;
import com.project.repository.SizeRepository;
import com.project.repository.ColorRepository;
import com.project.repository.SubcategoryRepository;
import com.project.service.ProductService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

@Service
public class ProductServiceImpl implements ProductService {
    private static final int IMPORT_COL_NAME = 0;
    private static final int IMPORT_COL_BARCODE = 1;
    private static final int IMPORT_COL_PRICE = 2;
    private static final int IMPORT_COL_ORIGINAL_PRICE = 3;
    private static final int IMPORT_COL_QUANTITY = 4;
    private static final int IMPORT_COL_CATEGORY = 5;
    private static final int IMPORT_COL_SUBCATEGORY = 6;
    private static final int IMPORT_COL_BRAND = 7;
    private static final int IMPORT_COL_DESCRIPTION = 8;
    private static final int IMPORT_COL_GENDER = 9;
    private static final int IMPORT_COL_RATING = 10;
    private static final int IMPORT_COL_REVIEWS = 11;
    private static final int IMPORT_COL_SIZES = 12;
    private static final int IMPORT_COL_COLORS = 13;
    private static final int IMPORT_COL_TAGS = 14;
    private static final int IMPORT_COL_IS_NEW = 15;
    private static final int IMPORT_COL_IS_LIMITED = 16;
    private static final int IMPORT_COL_IS_BESTSELLER = 17;

    @Override
    @Transactional
    public void importProductsFromExcel(java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        logger.info("importProductsFromExcel - start");
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files uploaded");
        }
        // Find the Excel or CSV file
        org.springframework.web.multipart.MultipartFile dataFile = files.stream()
            .filter(f -> f.getOriginalFilename() != null && (f.getOriginalFilename().endsWith(".xlsx") || f.getOriginalFilename().endsWith(".xls") || f.getOriginalFilename().endsWith(".csv")))
            .findFirst().orElseThrow(() -> new IllegalArgumentException("Excel or CSV file not found in upload"));

        if (dataFile.getOriginalFilename().endsWith(".csv")) {
            // CSV import using OpenCSV
            try (var reader = new java.io.InputStreamReader(dataFile.getInputStream());
                 var csv = new com.opencsv.CSVReader(reader)) {
                String[] header = csv.readNext();
                if (header == null) throw new IllegalArgumentException("CSV file is empty");
                java.util.Map<String, Integer> headerIndexes = buildHeaderIndexes(header);
                String[] row;
                int rowCount = 0;
                int rowNumber = 1;
                while ((row = csv.readNext()) != null) {
                    rowNumber++;
                    if (isCsvRowEmpty(row)) {
                        continue;
                    }
                    ProductRequest req = mapCsvRowToProductRequest(row, headerIndexes);
                    req = normalizeImportedProductRequest(req);
                    req = prepareImportedProductRequest(req, "CSV row " + rowNumber);
                    createProduct(req);
                    rowCount++;
                }
                logger.info("importProductsFromExcel - imported {} products from CSV", rowCount);
            } catch (Exception e) {
                logger.error("importProductsFromExcel - CSV error", e);
                throw propagateImportException(e);
            } finally {
                logger.debug("importProductsFromExcel - end");
            }
            return;
        }

        // Excel import (default)
        try (var inputStream = dataFile.getInputStream();
             var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(inputStream)) {
            var sheet = workbook.getSheetAt(0);
            java.util.Map<String, Integer> headerIndexes = buildHeaderIndexes(sheet.getRow(0));
            int rowCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row
                var row = sheet.getRow(i);
                if (row == null || isSpreadsheetRowEmpty(row)) continue;
                ProductRequest req = mapSpreadsheetRowToProductRequest(row, headerIndexes);
                req = normalizeImportedProductRequest(req);
                req = prepareImportedProductRequest(req, "Excel row " + (i + 1));
                createProduct(req);
                rowCount++;
            }
            logger.info("importProductsFromExcel - imported {} products from Excel", rowCount);
        } catch (Exception e) {
            logger.error("importProductsFromExcel - Excel error", e);
            throw propagateImportException(e);
        } finally {
            logger.debug("importProductsFromExcel - end");
        }
    }

    private RuntimeException propagateImportException(Exception exception) {
        return exception instanceof RuntimeException runtimeException
                ? runtimeException
                : new IllegalStateException(exception.getMessage(), exception);
    }

    private ProductRequest mapCsvRowToProductRequest(String[] row, java.util.Map<String, Integer> headerIndexes) {
        ProductRequest req = new ProductRequest();
        req.setName(getCsv(row, IMPORT_COL_NAME));
        String barcode = getCsv(row, IMPORT_COL_BARCODE);
        req.setBarcode(firstNonBlank(
                getCsv(row, headerIndexes, "barcode"),
                getCsv(row, headerIndexes, "sku"),
                barcode));
        req.setItemCode(firstNonBlank(
                getCsv(row, headerIndexes, "itemcode", "item_code", "code"),
                req.getBarcode()));
        req.setPrice(parseDouble(getCsv(row, IMPORT_COL_PRICE)));
        req.setOriginalPrice(parseDouble(getCsv(row, IMPORT_COL_ORIGINAL_PRICE)));
        req.setQuantity(parseInt(getCsv(row, IMPORT_COL_QUANTITY)));
        req.setCategoryName(getCsv(row, IMPORT_COL_CATEGORY));
        req.setSubcategoryName(getCsv(row, IMPORT_COL_SUBCATEGORY));
        req.setBrand(getCsv(row, IMPORT_COL_BRAND));
        req.setDescription(getCsv(row, IMPORT_COL_DESCRIPTION));
        req.setGender(getCsv(row, IMPORT_COL_GENDER));
        req.setRating(parseDouble(getCsv(row, IMPORT_COL_RATING)));
        req.setReviews(parseInt(getCsv(row, IMPORT_COL_REVIEWS)));
        req.setSizes(splitCsv(getCsv(row, IMPORT_COL_SIZES)));
        req.setColors(splitCsv(getCsv(row, IMPORT_COL_COLORS)));
        req.setTags(splitCsv(getCsv(row, IMPORT_COL_TAGS)));
        req.setIsNew(parseBoolean(getCsv(row, IMPORT_COL_IS_NEW)));
        req.setIsLimited(parseBoolean(getCsv(row, IMPORT_COL_IS_LIMITED)));
        req.setIsBestseller(parseBoolean(getCsv(row, IMPORT_COL_IS_BESTSELLER)));
        return req;
    }

    private ProductRequest mapSpreadsheetRowToProductRequest(org.apache.poi.ss.usermodel.Row row, java.util.Map<String, Integer> headerIndexes) {
        ProductRequest req = new ProductRequest();
        req.setName(getCellString(row, IMPORT_COL_NAME));
        String barcode = getCellString(row, IMPORT_COL_BARCODE);
        req.setBarcode(firstNonBlank(
                getCellString(row, headerIndexes, "barcode"),
                getCellString(row, headerIndexes, "sku"),
                barcode));
        req.setItemCode(firstNonBlank(
                getCellString(row, headerIndexes, "itemcode", "item_code", "code"),
                req.getBarcode()));
        req.setPrice(getCellDouble(row, IMPORT_COL_PRICE));
        req.setOriginalPrice(getCellDouble(row, IMPORT_COL_ORIGINAL_PRICE));
        req.setQuantity((int) getCellDouble(row, IMPORT_COL_QUANTITY));
        req.setCategoryName(getCellString(row, IMPORT_COL_CATEGORY));
        req.setSubcategoryName(getCellString(row, IMPORT_COL_SUBCATEGORY));
        req.setBrand(getCellString(row, IMPORT_COL_BRAND));
        req.setDescription(getCellString(row, IMPORT_COL_DESCRIPTION));
        req.setGender(getCellString(row, IMPORT_COL_GENDER));
        req.setRating(getCellDouble(row, IMPORT_COL_RATING));
        req.setReviews((int) getCellDouble(row, IMPORT_COL_REVIEWS));
        req.setSizes(splitCell(row, IMPORT_COL_SIZES));
        req.setColors(splitCell(row, IMPORT_COL_COLORS));
        req.setTags(splitCell(row, IMPORT_COL_TAGS));
        req.setIsNew(getCellBoolean(row, IMPORT_COL_IS_NEW));
        req.setIsLimited(getCellBoolean(row, IMPORT_COL_IS_LIMITED));
        req.setIsBestseller(getCellBoolean(row, IMPORT_COL_IS_BESTSELLER));
        return req;
    }

    private boolean isCsvRowEmpty(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }

        for (String value : row) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean isSpreadsheetRowEmpty(org.apache.poi.ss.usermodel.Row row) {
        if (row == null) {
            return true;
        }

        for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) {
            if (cellIndex < 0) {
                continue;
            }

            String cellValue = getCellString(row, cellIndex);
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    // CSV helpers
    private String getCsv(String[] row, int col) {
        return (col < row.length) ? row[col] : null;
    }
    private String getCsv(String[] row, java.util.Map<String, Integer> headerIndexes, String... aliases) {
        Integer columnIndex = findColumnIndex(headerIndexes, aliases);
        if (columnIndex == null) {
            return null;
        }

        return getCsv(row, columnIndex);
    }

    private java.util.Map<String, Integer> buildHeaderIndexes(String[] header) {
        java.util.Map<String, Integer> indexes = new java.util.HashMap<>();
        if (header == null) {
            return indexes;
        }

        for (int columnIndex = 0; columnIndex < header.length; columnIndex++) {
            String normalizedHeader = normalizeHeaderName(header[columnIndex]);
            if (!normalizedHeader.isEmpty()) {
                indexes.putIfAbsent(normalizedHeader, columnIndex);
            }
        }

        return indexes;
    }

    private java.util.Map<String, Integer> buildHeaderIndexes(org.apache.poi.ss.usermodel.Row headerRow) {
        java.util.Map<String, Integer> indexes = new java.util.HashMap<>();
        if (headerRow == null) {
            return indexes;
        }

        for (int columnIndex = headerRow.getFirstCellNum(); columnIndex < headerRow.getLastCellNum(); columnIndex++) {
            if (columnIndex < 0) {
                continue;
            }

            String normalizedHeader = normalizeHeaderName(getCellString(headerRow, columnIndex));
            if (!normalizedHeader.isEmpty()) {
                indexes.putIfAbsent(normalizedHeader, columnIndex);
            }
        }

        return indexes;
    }

    private Integer findColumnIndex(java.util.Map<String, Integer> headerIndexes, String... aliases) {
        if (headerIndexes == null || headerIndexes.isEmpty() || aliases == null) {
            return null;
        }

        for (String alias : aliases) {
            String normalizedAlias = normalizeHeaderName(alias);
            if (headerIndexes.containsKey(normalizedAlias)) {
                return headerIndexes.get(normalizedAlias);
            }
        }

        return null;
    }

    private String normalizeHeaderName(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }

        return null;
    }
    private Double parseDouble(String s) {
        try { return s == null ? null : Double.parseDouble(s); } catch (Exception e) { return null; }
    }
    private Integer parseInt(String s) {
        try { return s == null ? null : Integer.parseInt(s); } catch (Exception e) { return null; }
    }
    private Boolean parseBoolean(String s) {
        if (s == null) return null;
        s = s.trim().toLowerCase();
        if (s.equals("true")) return true;
        if (s.equals("false")) return false;
        return null;
    }
    private java.util.List<String> splitCsv(String s) {
        if (s == null || s.isBlank()) return java.util.Collections.emptyList();
        String[] parts = s.split("\\|");
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String p : parts) {
            if (!p.trim().isEmpty()) list.add(p.trim());
        }
        return list;
    }

    private String toBase64(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        return java.util.Base64.getEncoder().encodeToString(file.getBytes());
    }

    private java.util.List<String> splitCell(org.apache.poi.ss.usermodel.Row row, int col) {
        String val = getCellString(row, col);
        if (val == null || val.isBlank()) return java.util.Collections.emptyList();
        String[] parts = val.split("\\|");
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String p : parts) {
            if (!p.trim().isEmpty()) list.add(p.trim());
        }
        return list;
    }

    private Boolean getCellBoolean(org.apache.poi.ss.usermodel.Row row, int col) {
        String val = getCellString(row, col);
        if (val == null) return null;
        val = val.trim().toLowerCase();
        if (val.equals("true")) return true;
        if (val.equals("false")) return false;
        return null;
    }

        private String getCellString(org.apache.poi.ss.usermodel.Row row, int col) {
            var cell = row.getCell(col);
            return cell != null ? cell.toString().trim() : null;
        }

        private String getCellString(org.apache.poi.ss.usermodel.Row row, java.util.Map<String, Integer> headerIndexes, String... aliases) {
            Integer columnIndex = findColumnIndex(headerIndexes, aliases);
            if (columnIndex == null) {
                return null;
            }

            return getCellString(row, columnIndex);
        }

        private double getCellDouble(org.apache.poi.ss.usermodel.Row row, int col) {
            var cell = row.getCell(col);
            if (cell == null) return 0.0;
            try {
                return Double.parseDouble(cell.toString().trim());
            } catch (Exception e) {
                return 0.0;
            }
        }
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private static final String OTHER_FILTER_VALUE = "other";
    private static final List<String> STANDARD_GENDERS = List.of("men", "women", "unisex", "kids");
    private static final List<String> STORE_FEATURE_OPTIONS = List.of("sale", "new-arrival", "limited-edition", "bestseller");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

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
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProductsWithImagesPaged(int page, int size) {
        return getStoreProducts(null, null, null, "newest", null, null, null, null, null, null, null, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategoryIdPaged(Long categoryId, int page, int size) {
        return getStoreProducts(categoryId, null, null, "newest", null, null, null, null, null, null, null, page, size);
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
            product.setBarcode(normalizeNullableText(req.getBarcode()));
            product.setItemCode(normalizeNullableText(req.getItemCode()));
            product.setPrice(req.getPrice() != null ? req.getPrice() : 0.0);
            product.setOriginalPrice(req.getOriginalPrice());
            product.setDescription(req.getDescription());
            product.setGender(normalizeText(req.getGender()));
            product.setRating(req.getRating() != null ? req.getRating() : 0.0);
            product.setReviews(req.getReviews() != null ? req.getReviews() : 0);
            product.setQuantity(req.getQuantity());

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
            Long incomingSubcategoryId = req.getSubcategoryId();
            String incomingSubcategoryName = req.getSubcategoryName();

            // resolve category by id or name
            Category category = resolveCategoryEntity(incomingCategoryId, incomingCategoryName);
            Subcategory subcategory = resolveSubcategoryEntity(incomingSubcategoryId, category, incomingSubcategoryName);
            category = resolveCategoryForSubcategory(category, subcategory);
            product.setCategory(category);
            product.setSubcategory(subcategory);
            product.setBrand(resolveBrandEntity(req.getBrand()));

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
            if (req.getBarcode() != null) {
                product.setBarcode(normalizeNullableText(req.getBarcode()));
            }
            if (req.getItemCode() != null) {
                product.setItemCode(normalizeNullableText(req.getItemCode()));
            }
            product.setPrice(req.getPrice() != null ? req.getPrice() : product.getPrice());
            product.setOriginalPrice(req.getOriginalPrice() != null ? req.getOriginalPrice() : product.getOriginalPrice());
            product.setDescription(req.getDescription() != null ? req.getDescription() : product.getDescription());
            product.setGender(req.getGender() != null ? normalizeText(req.getGender()) : product.getGender());
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
            Long incomingSubcategoryId = req.getSubcategoryId();
                String incomingSubcategoryName = req.getSubcategoryName();


            // resolve category
            Category category = resolveCategoryEntity(incomingCategoryId, incomingCategoryName);
                Subcategory subcategory = incomingSubcategoryId != null || !isBlank(incomingSubcategoryName)
                    ? resolveSubcategoryEntity(incomingSubcategoryId, category != null ? category : product.getCategory(), incomingSubcategoryName)
                    : product.getSubcategory();
            category = resolveCategoryForSubcategory(category, subcategory);
            product.setCategory(category);
            product.setSubcategory(subcategory);
            if (req.getBrand() != null || product.getBrand() == null) {
                product.setBrand(resolveBrandEntity(req.getBrand()));
            }

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
            List<ProductDto> dtos = getStoreProducts(null, null, null, "newest", null, null, null, null, null, null, null, 0, Integer.MAX_VALUE).getContent();
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
            List<ProductDto> dtos = getStoreProducts(categoryId, null, null, "newest", null, null, null, null, null, null, null, 0, Integer.MAX_VALUE).getContent();
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
    public Page<ProductDto> getStoreProducts(Long categoryId, String search, String subcategory, String sortBy, Double minPrice, Double maxPrice, List<String> genders, List<String> brands, List<String> sizes, List<String> colors, List<String> features, int page, int size) {
        logger.info("getStoreProducts - start categoryId={} search={} subcategory={} sortBy={} page={} size={}", categoryId, search, subcategory, sortBy, page, size);
        return queryProducts(categoryId, search, subcategory, sortBy, minPrice, maxPrice, genders, brands, sizes, colors, features, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreProductFiltersDto getStoreProductFilters(Long categoryId) {
        logger.info("getStoreProductFilters - start categoryId={}", categoryId);
        List<Product> products = productRepository.findAll(Specification.where(hasCategory(categoryId)));

        Set<String> genders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> brands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> sizes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> colors = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        boolean hasOtherGender = false;
        boolean hasOtherBrand = false;

        for (Product product : products) {
            if (isOtherGender(product.getGender())) {
                hasOtherGender = true;
            } else {
                addIfPresent(genders, product.getGender());
            }

            String brandName = product.getBrand() != null ? product.getBrand().getName() : null;
            if (brandName == null || brandName.trim().isEmpty()) {
                hasOtherBrand = true;
            } else {
                addIfPresent(brands, brandName);
            }

            collectValues(sizes, product.getSizes() == null ? List.of() : product.getSizes().stream().map(Size::getValue).toList());
            collectValues(colors, product.getColors() == null ? List.of() : product.getColors().stream().map(Color::getValue).toList());
        }

        if (hasOtherGender) {
            genders.add(OTHER_FILTER_VALUE);
        }
        if (hasOtherBrand) {
            brands.add(OTHER_FILTER_VALUE);
        }

        return new StoreProductFiltersDto(
            products.stream().map(Product::getPrice).min(Double::compareTo).orElse(0.0),
            products.stream().map(Product::getPrice).max(Double::compareTo).orElse(0.0),
                new ArrayList<>(genders),
                new ArrayList<>(brands),
                new ArrayList<>(sizes),
                new ArrayList<>(colors),
                STORE_FEATURE_OPTIONS,
                List.of("newest", "popular", "rating", "price-low", "price-high")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAdminProducts(Long categoryId, String search, String sortBy, int page, int size) {
        logger.info("getAdminProducts - start categoryId={} search={} sortBy={} page={} size={}", categoryId, search, sortBy, page, size);
        return queryProducts(categoryId, search, null, sortBy, null, null, null, null, null, null, null, page, size);
    }

    private Page<ProductDto> queryProducts(Long categoryId, String search, String subcategory, String sortBy, Double minPrice, Double maxPrice, List<String> genders, List<String> brands, List<String> sizes, List<String> colors, List<String> features, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, resolveSort(sortBy));
        Specification<Product> specification = Specification.where(hasCategory(categoryId))
                .and(matchesSearch(search))
                .and(matchesSubcategory(subcategory))
                .and(matchesPriceRange(minPrice, maxPrice))
                .and(matchesGenderValues(genders))
                .and(matchesBrandValues(brands))
                .and(matchesCollectionValues("sizes", "value", sizes))
                .and(matchesCollectionValues("colors", "value", colors))
                .and(matchesFeatureFlags(features));
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
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    private Specification<Product> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            if (query != null) {
                query.distinct(true);
            }

            String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                Join<Product, Subcategory> subcategoryJoin = root.join("subcategory", JoinType.LEFT);
            Join<Product, Brand> brandJoin = root.join("brand", JoinType.LEFT);
            Join<Product, Tag> tagJoin = root.join("tags", JoinType.LEFT);
            Join<Product, Color> colorJoin = root.join("colors", JoinType.LEFT);
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("barcode")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("itemCode")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(subcategoryJoin.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(brandJoin.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(tagJoin.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(colorJoin.get("value")), pattern)
            );
        };
    }

    private Specification<Product> matchesSubcategory(String subcategory) {
        return (root, query, criteriaBuilder) -> {
            if (subcategory == null || subcategory.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<Product, Subcategory> subcategoryJoin = root.join("subcategory", JoinType.LEFT);
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(subcategoryJoin.get("name")),
                    subcategory.trim().toLowerCase(Locale.ROOT)
            );
        };
    }

    private Specification<Product> matchesPriceRange(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates.size() == 1
                    ? predicates.get(0)
                    : criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Specification<Product> matchesScalarValues(String fieldName, List<String> rawValues) {
        List<String> normalizedValues = normalizeFilterValues(rawValues);
        return (root, query, criteriaBuilder) -> {
            if (normalizedValues.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lower(root.get(fieldName)).in(normalizedValues);
        };
    }

    private Specification<Product> matchesGenderValues(List<String> rawValues) {
        List<String> normalizedValues = new ArrayList<>(normalizeFilterValues(rawValues));
        boolean includeOther = normalizedValues.remove(OTHER_FILTER_VALUE);

        return (root, query, criteriaBuilder) -> {
            if (normalizedValues.isEmpty() && !includeOther) {
                return criteriaBuilder.conjunction();
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (!normalizedValues.isEmpty()) {
                predicates.add(criteriaBuilder.lower(root.get("gender")).in(normalizedValues));
            }

            if (includeOther) {
                jakarta.persistence.criteria.Expression<String> loweredGender = criteriaBuilder.lower(root.get("gender"));
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("gender")),
                        criteriaBuilder.equal(loweredGender, ""),
                        criteriaBuilder.not(loweredGender.in(STANDARD_GENDERS))
                ));
            }

            return predicates.size() == 1 ? predicates.get(0) : criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Specification<Product> matchesBrandValues(List<String> rawValues) {
        List<String> normalizedValues = new ArrayList<>(normalizeFilterValues(rawValues));
        boolean includeOther = normalizedValues.remove(OTHER_FILTER_VALUE);

        return (root, query, criteriaBuilder) -> {
            if (normalizedValues.isEmpty() && !includeOther) {
                return criteriaBuilder.conjunction();
            }

            Join<Product, Brand> brandJoin = root.join("brand", JoinType.LEFT);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (!normalizedValues.isEmpty()) {
                predicates.add(criteriaBuilder.lower(brandJoin.get("name")).in(normalizedValues));
            }

            if (includeOther) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("brand")),
                        criteriaBuilder.isNull(brandJoin.get("name")),
                        criteriaBuilder.equal(criteriaBuilder.lower(brandJoin.get("name")), "")
                ));
            }

            return predicates.size() == 1 ? predicates.get(0) : criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Specification<Product> matchesRelationValues(String relationName, String fieldName, List<String> rawValues) {
        List<String> normalizedValues = normalizeFilterValues(rawValues);
        return (root, query, criteriaBuilder) -> {
            if (normalizedValues.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Product, ?> join = root.join(relationName, JoinType.LEFT);
            return criteriaBuilder.lower(join.get(fieldName)).in(normalizedValues);
        };
    }

    private Specification<Product> matchesCollectionValues(String relationName, String fieldName, List<String> rawValues) {
        List<String> normalizedValues = normalizeFilterValues(rawValues);
        return (root, query, criteriaBuilder) -> {
            if (normalizedValues.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            Join<Product, ?> join = root.join(relationName, JoinType.LEFT);
            return criteriaBuilder.lower(join.get(fieldName)).in(normalizedValues);
        };
    }

    private Specification<Product> matchesFeatureFlags(List<String> rawValues) {
        List<String> normalizedValues = normalizeFilterValues(rawValues);

        return (root, query, criteriaBuilder) -> {
            if (normalizedValues.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            for (String value : normalizedValues) {
                switch (value) {
                    case "sale" -> predicates.add(criteriaBuilder.and(
                            criteriaBuilder.isNotNull(root.get("originalPrice")),
                            criteriaBuilder.greaterThan(root.get("originalPrice"), root.get("price"))
                    ));
                    case "new-arrival" -> predicates.add(criteriaBuilder.isTrue(root.get("isNew")));
                    case "limited-edition" -> predicates.add(criteriaBuilder.isTrue(root.get("isLimited")));
                    case "bestseller" -> predicates.add(criteriaBuilder.isTrue(root.get("isBestseller")));
                    default -> {
                    }
                }
            }

            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            return predicates.size() == 1 ? predicates.get(0) : criteriaBuilder.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
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

    private boolean isOtherGender(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        return !STANDARD_GENDERS.contains(value.trim().toLowerCase(Locale.ROOT));
    }

    private ProductDto mapToDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setBarcode(p.getBarcode());
        dto.setItemCode(p.getItemCode());
        dto.setPrice(p.getPrice());
        dto.setOriginalPrice(p.getOriginalPrice());
        dto.setDescription(p.getDescription());
        dto.setGender(p.getGender());
        dto.setBrand(p.getBrand() != null ? p.getBrand().getName() : null);
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
        dto.setSubcategoryId(mapSubcategoryId(p));
        dto.setSubcategory(mapSubcategoryName(p));
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

    private Long mapSubcategoryId(Product product) {
        if (product == null || product.getSubcategory() == null) {
            return null;
        }

        Object subcategoryIdentifier = entityManagerFactory
                .getPersistenceUnitUtil()
                .getIdentifier(product.getSubcategory());
        return subcategoryIdentifier instanceof Long ? (Long) subcategoryIdentifier : null;
    }

    private String mapSubcategoryName(Product product) {
        if (product == null || product.getSubcategory() == null) {
            return null;
        }

        try {
            return product.getSubcategory().getName();
        } catch (EntityNotFoundException exception) {
            logger.warn("Product {} references missing subcategory {}", product.getId(), mapSubcategoryId(product));
            return null;
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
        if (creating && isBlank(req.getGender())) {
            throw new IllegalArgumentException("Product gender is required.");
        }
        if (creating && isBlank(req.getBrand())) {
            throw new IllegalArgumentException("Product brand is required.");
        }
        if (!creating && req.getGender() != null && isBlank(req.getGender())) {
            throw new IllegalArgumentException("Product gender cannot be blank.");
        }
        if (!creating && req.getBrand() != null && isBlank(req.getBrand())) {
            throw new IllegalArgumentException("Product brand cannot be blank.");
        }
        if (req.getBarcode() != null && req.getBarcode().trim().isEmpty()) {
            throw new IllegalArgumentException("Product barcode cannot be blank.");
        }
        if (req.getItemCode() != null && req.getItemCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Product item code cannot be blank.");
        }
        if (req.getSubcategoryId() == null && isBlank(req.getSubcategoryName())) {
            throw new IllegalArgumentException("Product subcategory is required.");
        }
        if (req.getSubcategoryId() != null && req.getSubcategoryId() <= 0) {
            throw new IllegalArgumentException("Product subcategory is invalid.");
        }
        if (!isBlank(req.getSubcategoryName()) && req.getCategoryId() == null && isBlank(req.getCategoryName())) {
            throw new IllegalArgumentException("Product category is required when subcategory name is provided.");
        }
//        if (creating && (req.getImagesBase64() == null || req.getImagesBase64().isEmpty())) {
//            throw new IllegalArgumentException("At least one product image is required.");
//        }
    }

    private Category resolveCategoryEntity(Long categoryId, String categoryName) {
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        if (category == null && categoryName != null && !categoryName.isBlank()) {
            String normalizedCategoryName = normalizeNullableText(categoryName);
            category = categoryRepository.findByNameIgnoreCase(normalizedCategoryName).orElse(null);
            if (category == null) {
                requireAuthority("categories.create", "You do not have permission to create categories.");
                Category createdCategory = new Category();
                createdCategory.setName(normalizedCategoryName);
                category = categoryRepository.save(createdCategory);
                logger.info("Created new category id={} name={}", category.getId(), category.getName());
            }
        }
        return category;
    }

    private Category resolveCategoryForSubcategory(Category category, Subcategory subcategory) {
        if (subcategory == null) {
            return category;
        }

        if (subcategory.getCategory() == null || subcategory.getCategory().getId() == null) {
            throw new IllegalArgumentException("Selected subcategory is missing its parent category.");
        }

        Long subcategoryCategoryId = subcategory.getCategory().getId();
        if (category == null) {
            return subcategory.getCategory();
        }

        if (!category.getId().equals(subcategoryCategoryId)) {
            throw new IllegalArgumentException("Selected subcategory does not belong to the selected category.");
        }

        return category;
    }

    private Subcategory resolveSubcategoryEntity(Long subcategoryId, Category category, String subcategoryName) {
        if (subcategoryId != null) {
            return subcategoryRepository.findById(subcategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Subcategory not found."));
        }

        String normalizedSubcategoryName = normalizeNullableText(subcategoryName);
        if (normalizedSubcategoryName == null || normalizedSubcategoryName.isBlank()) {
            return null;
        }

        if (category == null || category.getId() == null) {
            throw new IllegalArgumentException("Product category is required when subcategory name is provided.");
        }

        return subcategoryRepository.findByCategory_IdAndNameIgnoreCase(category.getId(), normalizedSubcategoryName)
                .orElseGet(() -> {
                    requireAuthority("categories.create", "You do not have permission to create subcategories.");
                    Subcategory subcategory = new Subcategory();
                    subcategory.setName(normalizedSubcategoryName);
                    subcategory.setCategory(category);
                    Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
                    logger.info("Created new subcategory id={} name={} for categoryId={}", savedSubcategory.getId(), savedSubcategory.getName(), category.getId());
                    return savedSubcategory;
                });
    }

    private ProductRequest normalizeImportedProductRequest(ProductRequest request) {
        request.setName(normalizeLowercaseText(request.getName()));
        request.setBarcode(normalizeLowercaseText(request.getBarcode()));
        request.setItemCode(normalizeLowercaseText(request.getItemCode()));
        request.setCategoryName(normalizeLowercaseText(request.getCategoryName()));
        request.setSubcategoryName(normalizeLowercaseText(request.getSubcategoryName()));
        request.setBrand(normalizeLowercaseText(request.getBrand()));
        request.setDescription(normalizeLowercaseText(request.getDescription()));
        request.setGender(normalizeLowercaseText(request.getGender()));
        request.setSizes(normalizeTextList(request.getSizes()));
        request.setColors(normalizeTextList(request.getColors()));
        request.setTags(normalizeTextList(request.getTags()));
        return request;
    }

    private ProductRequest prepareImportedProductRequest(ProductRequest request, String rowLabel) {
        String categoryName = normalizeNullableText(request.getCategoryName());
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException(rowLabel + ": category is missing. Expected columns: name, barcode, price, originalPrice, quantity, category, subcategory, brand, description, gender, rating, reviews, sizes, colors, tags, isNew, isLimited, isBestseller, with optional itemCode/code.");
        }

        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new IllegalArgumentException(rowLabel + ": category '" + categoryName + "' is not available in the database."));

        String subcategoryName = normalizeNullableText(request.getSubcategoryName());
        if (subcategoryName == null || subcategoryName.isBlank()) {
            throw new IllegalArgumentException(rowLabel + ": subcategory is missing.");
        }

        Subcategory subcategory = subcategoryRepository.findByCategory_IdAndNameIgnoreCase(category.getId(), subcategoryName)
                .orElseThrow(() -> new IllegalArgumentException(rowLabel + ": subcategory '" + subcategoryName + "' is not available in the database for category '" + category.getName() + "'."));

        request.setCategoryId(category.getId());
        request.setCategoryName(category.getName());
        request.setSubcategoryId(subcategory.getId());
        request.setSubcategoryName(subcategory.getName());
        return request;
    }

    private List<String> normalizeTextList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<String> normalizedValues = new ArrayList<>();
        for (String value : values) {
            String normalizedValue = normalizeLowercaseText(value);
            if (normalizedValue != null && !normalizedValue.isBlank()) {
                normalizedValues.add(normalizedValue);
            }
        }

        return normalizedValues;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private List<String> normalizeFilterValues(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }

            for (String splitValue : rawValue.split(",")) {
                String trimmed = splitValue.trim().toLowerCase(Locale.ROOT);
                if (!trimmed.isEmpty()) {
                    normalized.add(trimmed);
                }
            }
        }

        return new ArrayList<>(normalized);
    }

    private void addIfPresent(Collection<String> target, String value) {
        if (!isBlank(value)) {
            target.add(value.trim());
        }
    }

    private void collectValues(Collection<String> target, Collection<String> values) {
        for (String value : values) {
            addIfPresent(target, value);
        }
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeLowercaseText(String value) {
        String normalizedValue = normalizeNullableText(value);
        return normalizedValue == null ? null : normalizedValue.toLowerCase(Locale.ROOT);
    }

    private Brand resolveBrandEntity(String requestedBrand) {
        String normalizedBrand = normalizeText(requestedBrand);
        if (normalizedBrand == null || normalizedBrand.isBlank()) {
            throw new IllegalArgumentException("Product brand is required.");
        }
        return brandRepository.findByNameIgnoreCase(normalizedBrand)
                .orElseGet(() -> {
                    requireAuthority("brands.create", "You do not have permission to create brands.");
                    return brandRepository.save(new Brand(null, normalizedBrand, null));
                });
    }

    private void requireAuthority(String authority, String message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()))) {
            throw new AccessDeniedException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
