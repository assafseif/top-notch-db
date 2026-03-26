package com.project.service.impl;

import com.project.dto.HeroBannerRequest;
import com.project.entity.HeroBanner;
import com.project.entity.Image;
import com.project.repository.HeroBannerRepository;
import com.project.repository.ImageRepository;
import com.project.service.HeroBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class HeroBannerServiceImpl implements HeroBannerService {
    private static final Logger logger = LoggerFactory.getLogger(HeroBannerServiceImpl.class);

    @Autowired
    private HeroBannerRepository heroBannerRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    @Transactional
    public HeroBanner createOrUpdateHeroBanner(HeroBannerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Hero banner details are required.");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Hero banner title is required.");
        }

        HeroBanner banner = request.getId() != null
                ? heroBannerRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Hero banner not found."))
                : new HeroBanner();

        List<Image> imgs = new ArrayList<>();
        if (request.getImagesBase64() != null) {
            for (String base64 : request.getImagesBase64()) {
                if (base64 == null || base64.isBlank()) continue;
                try {
                    byte[] decoded = Base64.getDecoder().decode(base64.replaceFirst("^data:.*;base64,", ""));
                    Image img = new Image();
                    img.setData(decoded);
                    imgs.add(img);
                } catch (IllegalArgumentException ex) {
                    logger.warn("Failed to decode one base64 image - skipping", ex);
                }
            }
        }
        if (!imgs.isEmpty()) {
            List<Image> savedImages = imageRepository.saveAll(imgs);
            banner.setImages(savedImages);
        }
        banner.setSubtitle(request.getSubtitle());
        banner.setTitle(request.getTitle());
        banner.setDescription(request.getDescription());
        banner.setCta(request.getCta());
        banner.setLink(request.getLink());
        return heroBannerRepository.save(banner);
    }

    @Override
    public List<HeroBanner> getAllHeroBanners() {
        return heroBannerRepository.findAll();
    }

    @Override
    public HeroBanner getHeroBanner(Long id) {
        return heroBannerRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deleteHeroBanner(Long id) {
        heroBannerRepository.deleteById(id);
    }
}
