package com.project.repository;

import com.project.entity.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {
    Optional<Subcategory> findByCategory_IdAndNameIgnoreCase(Long categoryId, String name);
    boolean existsByCategory_IdAndNameIgnoreCase(Long categoryId, String name);
}