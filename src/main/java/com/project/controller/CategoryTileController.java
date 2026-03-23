package com.project.controller;

import com.project.dto.CategoryTileRequest;
import com.project.entity.CategoryTile;
import com.project.service.CategoryTileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/category-tiles")
public class CategoryTileController {
    @Autowired
    private CategoryTileService categoryTileService;

    @PostMapping
    public CategoryTile createOrUpdateCategoryTile(@RequestBody CategoryTileRequest request) {
        return categoryTileService.createOrUpdateCategoryTile(request);
    }

    @GetMapping
    public List<CategoryTile> getAllCategoryTiles() {
        return categoryTileService.getAllCategoryTiles();
    }

    @GetMapping("/{id}")
    public CategoryTile getCategoryTile(@PathVariable Long id) {
        return categoryTileService.getCategoryTile(id);
    }
    @DeleteMapping("/{id}")
    public void deleteCategoryTile(@PathVariable Long id) {
        categoryTileService.deleteCategoryTile(id);
    }
}
