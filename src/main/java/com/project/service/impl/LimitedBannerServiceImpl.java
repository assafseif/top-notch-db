package com.project.service.impl;

import com.project.dto.LimitedBannerRequest;
import com.project.entity.LimitedBanner;
import com.project.entity.Image;
import com.project.repository.LimitedBannerRepository;
import com.project.repository.ImageRepository;
import com.project.service.LimitedBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

@Service
public class LimitedBannerServiceImpl implements LimitedBannerService {
    @Autowired
    private LimitedBannerRepository limitedBannerRepository;
    @Autowired
    private ImageRepository imageRepository;

    @Override
    public LimitedBanner createOrUpdateLimitedBanner(LimitedBannerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Limited banner details are required.");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Limited banner title is required.");
        }

        LimitedBanner banner = request.getId() != null
                ? limitedBannerRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Limited banner not found."))
                : new LimitedBanner();

        banner.setBadge(request.getBadge());
        banner.setTitle(request.getTitle());
        banner.setDescription(request.getDescription());
        banner.setCta(request.getCta());
        banner.setLink(request.getLink());
        // Convert base64 image to Image entity (accept both data URL and raw base64)
        if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            try {
                Image img = new Image();
                String base64 = request.getImageBase64();
                String cleanBase64 = base64;
                // If data URL, extract base64 part and content type
                if (base64.startsWith("data:")) {
                    String[] parts = base64.split(",", 2);
                    if (parts.length == 2) {
                        cleanBase64 = parts[1];
                        img.setContentType(parts[0].replace("data:", "").replace(";base64", ""));
                    }
                }
                img.setData(Base64.getDecoder().decode(cleanBase64));
                banner.setImage(imageRepository.save(img));
            } catch (IllegalArgumentException ex) {
                // log and skip invalid base64
            }
        }
        return limitedBannerRepository.save(banner);
    }

    @Override
    public List<LimitedBanner> getAllLimitedBanners() {
        return limitedBannerRepository.findAll();
    }

    @Override
    public LimitedBanner getLimitedBanner(Long id) {
        return limitedBannerRepository.findById(id).orElse(null);
    }
    @Override
    public void deleteLimitedBanner(Long id) {
        limitedBannerRepository.deleteById(id);
    }
}
