package com.project.service;

import com.project.dto.CategoryTileRequest;
import com.project.entity.CategoryTile;
import java.util.List;

public interface CategoryTileService {
    CategoryTile createOrUpdateCategoryTile(CategoryTileRequest request);
    List<CategoryTile> getAllCategoryTiles();
    CategoryTile getCategoryTile(Long id);
    void deleteCategoryTile(Long id);
}
