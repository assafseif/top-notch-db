package com.project.service.impl;

import com.project.entity.Brand;
import com.project.repository.BrandRepository;
import com.project.repository.ProductRepository;
import com.project.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    private static final Logger logger = LoggerFactory.getLogger(BrandServiceImpl.class);

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllBrands() {
        logger.info("getAllBrands - start");
        try {
            List<String> brands = brandRepository.findAllByOrderByNameAsc().stream()
                    .map(Brand::getName)
                    .toList();
            logger.info("getAllBrands - found {} brands", brands.size());
            return brands;
        } finally {
            logger.debug("getAllBrands - end");
        }
    }

    @Override
    @Transactional
    public String createBrand(String name) {
        String normalizedName = normalizeName(name, "Brand name is required.");

        if (brandRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Brand name already exists.");
        }

        Brand saved = brandRepository.save(new Brand(null, normalizedName, null));
        logger.info("createBrand - saved id={} name={}", saved.getId(), saved.getName());
        return saved.getName();
    }

    @Override
    @Transactional
    public long renameBrand(String currentName, String newName) {
        String normalizedCurrentName = normalizeName(currentName, "Current brand name is required.");
        String normalizedNewName = normalizeName(newName, "New brand name is required.");

        logger.info("renameBrand - start currentName={} newName={}", normalizedCurrentName, normalizedNewName);
        try {
            Brand brand = brandRepository.findByNameIgnoreCase(normalizedCurrentName)
                    .orElseThrow(() -> new IllegalArgumentException("Brand not found."));
            if (brandRepository.existsByNameIgnoreCase(normalizedNewName)
                    && !brand.getName().equalsIgnoreCase(normalizedNewName)) {
                throw new IllegalArgumentException("Brand name already exists.");
            }

            long updatedCount = productRepository.countByBrand_Id(brand.getId());
            brand.setName(normalizedNewName);
            brandRepository.save(brand);
            logger.info("renameBrand - updated {} products", updatedCount);
            return updatedCount;
        } finally {
            logger.debug("renameBrand - end");
        }
    }

    private String normalizeName(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value.trim();
    }
}