package com.project.controller;

import com.project.dto.HeroBannerRequest;
import com.project.entity.HeroBanner;
import com.project.service.HeroBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hero-banners")
public class HeroBannerController {
    @Autowired
    private HeroBannerService heroBannerService;

    @PostMapping
    @PreAuthorize("hasAuthority('homepage.edit')")
    public HeroBanner createOrUpdateHeroBanner(@RequestBody HeroBannerRequest request) {
        return heroBannerService.createOrUpdateHeroBanner(request);
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('homepage.view')")
    public List<HeroBanner> getAllHeroBanners() {
        return heroBannerService.getAllHeroBanners();
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('homepage.view')")
    public HeroBanner getHeroBanner(@PathVariable Long id) {
        return heroBannerService.getHeroBanner(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('homepage.edit')")
    public void deleteHeroBanner(@PathVariable Long id) {
        heroBannerService.deleteHeroBanner(id);
    }
}
