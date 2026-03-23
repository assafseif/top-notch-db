package com.project.controller;

import com.project.dto.LimitedBannerRequest;
import com.project.entity.LimitedBanner;
import com.project.service.LimitedBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/limited-banner")
public class LimitedBannerController {
    @Autowired
    private LimitedBannerService limitedBannerService;

    @PostMapping
    public LimitedBanner createOrUpdateLimitedBanner(@RequestBody LimitedBannerRequest request) {
        return limitedBannerService.createOrUpdateLimitedBanner(request);
    }

    @GetMapping
    public List<LimitedBanner> getAllLimitedBanners() {
        return limitedBannerService.getAllLimitedBanners();
    }

    @GetMapping("/{id}")
    public LimitedBanner getLimitedBanner(@PathVariable Long id) {
        return limitedBannerService.getLimitedBanner(id);
    }
    @DeleteMapping("/{id}")
    public void deleteLimitedBanner(@PathVariable Long id) {
        limitedBannerService.deleteLimitedBanner(id);
    }
}
