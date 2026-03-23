package com.project.service;

import com.project.dto.LimitedBannerRequest;
import com.project.entity.LimitedBanner;
import java.util.List;

public interface LimitedBannerService {
    LimitedBanner createOrUpdateLimitedBanner(LimitedBannerRequest request);
    List<LimitedBanner> getAllLimitedBanners();
    LimitedBanner getLimitedBanner(Long id);
    void deleteLimitedBanner(Long id);
}
