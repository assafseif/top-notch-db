package com.project.repository;

import com.project.entity.CategoryTile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryTileRepository extends JpaRepository<CategoryTile, Long> {
}
