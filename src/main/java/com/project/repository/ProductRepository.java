package com.project.repository;

import com.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // find all products that belong to a category by its id
    List<Product> findAllByCategory_Id(Long categoryId);

    // paginated
    Page<Product> findAll(Pageable pageable);
    Page<Product> findAllByCategory_Id(Long categoryId, Pageable pageable);
}
