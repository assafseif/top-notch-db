package com.project.service;

import com.project.dto.HeroBannerRequest;
import com.project.entity.HeroBanner;
import java.util.List;

public interface HeroBannerService {
    HeroBanner createOrUpdateHeroBanner(HeroBannerRequest request);
    List<HeroBanner> getAllHeroBanners();
    HeroBanner getHeroBanner(Long id);
}
