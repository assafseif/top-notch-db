package com.project.service;

import com.project.dto.ProductRequest;
import com.project.dto.ProductCategoryDto;
import com.project.dto.ProductDto;
import com.project.dto.StoreProductFiltersDto;
import com.project.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    void importProductsFromExcel(java.util.List<org.springframework.web.multipart.MultipartFile> files);
    List<Product> getAllProducts();
    Product getProduct(Long id);
    Product createProduct(ProductRequest req);
    Product updateProduct(Long id, ProductRequest req);
    void deleteProduct(Long id);

    // new method to return product-category DTOs
    List<ProductCategoryDto> getAllProductCategories();

    // methods that return ProductDto including base64 images
    List<ProductDto> getAllProductsWithImages();
    ProductDto getProductWithImages(Long id);
    List<ProductDto> getProductsByCategoryId(Long categoryId);
    Page<ProductDto> getStoreProducts(Long categoryId, String search, String subcategory, String sortBy, Double minPrice, Double maxPrice, List<String> genders, List<String> brands, List<String> sizes, List<String> colors, List<String> features, int page, int size);
    StoreProductFiltersDto getStoreProductFilters(Long categoryId);
    Page<ProductDto> getAdminProducts(Long categoryId, String search, String sortBy, int page, int size);

    // PAGINATED versions
    Page<ProductDto> getAllProductsWithImagesPaged(int page, int size);
    Page<ProductDto> getProductsByCategoryIdPaged(Long categoryId, int page, int size);
}
