package com.project.service.impl;

import com.project.dto.CategoryTileRequest;
import com.project.entity.CategoryTile;
import com.project.entity.Image;
import com.project.repository.CategoryTileRepository;
import com.project.repository.ImageRepository;
import com.project.service.CategoryTileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryTileServiceImpl implements CategoryTileService {
    @Autowired
    private CategoryTileRepository categoryTileRepository;
    @Autowired
    private ImageRepository imageRepository;

    @Override
    public CategoryTile createOrUpdateCategoryTile(CategoryTileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Category tile details are required.");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category tile name is required.");
        }

        CategoryTile tile = request.getId() != null
                ? categoryTileRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category tile not found."))
                : new CategoryTile();

        tile.setName(request.getName());
        tile.setLink(request.getLink());
        // Convert base64 image to Image entity (accept both data URL and raw base64)
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            try {
                Image img = new Image();
                String base64 = request.getImageBase64();
                String cleanBase64 = base64;
                if (base64.startsWith("data:")) {
                    String[] parts = base64.split(",", 2);
                    if (parts.length == 2) {
                        cleanBase64 = parts[1];
                        img.setContentType(parts[0].replace("data:", "").replace(";base64", ""));
                    }
                }
                img.setData(Base64.getDecoder().decode(cleanBase64));
                tile.setImage(imageRepository.save(img));
            } catch (IllegalArgumentException ex) {
                // log and skip invalid base64
            }
        }
        return categoryTileRepository.save(tile);
    }

    @Override
    public List<CategoryTile> getAllCategoryTiles() {
        return categoryTileRepository.findAll();
    }

    @Override
    public CategoryTile getCategoryTile(Long id) {
        return categoryTileRepository.findById(id).orElse(null);
    }
    @Override
    public void deleteCategoryTile(Long id) {
        categoryTileRepository.deleteById(id);
    }
}
