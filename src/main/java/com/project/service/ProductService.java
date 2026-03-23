package com.project.service;

import com.project.dto.ProductRequest;
import com.project.dto.ProductCategoryDto;
import com.project.dto.ProductDto;
import com.project.entity.Product;

import java.util.List;

public interface ProductService {
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

    // PAGINATED versions
    org.springframework.data.domain.Page<ProductDto> getAllProductsWithImagesPaged(int page, int size);
    org.springframework.data.domain.Page<ProductDto> getProductsByCategoryIdPaged(Long categoryId, int page, int size);
}
