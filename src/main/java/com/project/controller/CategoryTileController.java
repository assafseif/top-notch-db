package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.CategoryTileRequest;
import com.project.entity.CategoryTile;
import com.project.service.CategoryTileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/category-tiles")
public class CategoryTileController {
    @Autowired
    private CategoryTileService categoryTileService;

    @PostMapping
    @PreAuthorize("hasAuthority('homepage.edit')")
    public ApiResponse<CategoryTile> createOrUpdateCategoryTile(@RequestBody CategoryTileRequest request) {
        String message = request != null && request.getId() != null
                ? "Category tile updated successfully."
                : "Category tile created successfully.";
        return ApiResponse.of(message, categoryTileService.createOrUpdateCategoryTile(request));
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('homepage.view')")
    public List<CategoryTile> getAllCategoryTiles() {
        return categoryTileService.getAllCategoryTiles();
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('homepage.view')")
    public CategoryTile getCategoryTile(@PathVariable Long id) {
        return categoryTileService.getCategoryTile(id);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('homepage.edit')")
    public ApiResponse<Void> deleteCategoryTile(@PathVariable Long id) {
        categoryTileService.deleteCategoryTile(id);
        return ApiResponse.of("Category tile deleted successfully.");
    }
}
