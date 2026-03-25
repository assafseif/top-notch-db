package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.LimitedBannerRequest;
import com.project.entity.LimitedBanner;
import com.project.service.LimitedBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/limited-banner")
public class LimitedBannerController {
    @Autowired
    private LimitedBannerService limitedBannerService;

    @PostMapping
    @PreAuthorize("hasAuthority('homepage.edit')")
    public ApiResponse<LimitedBanner> createOrUpdateLimitedBanner(@RequestBody LimitedBannerRequest request) {
        String message = request != null && request.getId() != null
                ? "Limited banner updated successfully."
                : "Limited banner created successfully.";
        return ApiResponse.of(message, limitedBannerService.createOrUpdateLimitedBanner(request));
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('homepage.view')")
    public List<LimitedBanner> getAllLimitedBanners() {
        return limitedBannerService.getAllLimitedBanners();
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('homepage.view')")
    public LimitedBanner getLimitedBanner(@PathVariable Long id) {
        return limitedBannerService.getLimitedBanner(id);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('homepage.edit')")
    public ApiResponse<Void> deleteLimitedBanner(@PathVariable Long id) {
        limitedBannerService.deleteLimitedBanner(id);
        return ApiResponse.of("Limited banner deleted successfully.");
    }
}
