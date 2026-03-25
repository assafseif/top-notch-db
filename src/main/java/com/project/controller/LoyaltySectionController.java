package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.LoyaltySectionRequest;
import com.project.entity.LoyaltySection;
import com.project.service.LoyaltySectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty-section")
public class LoyaltySectionController {
    @Autowired
    private LoyaltySectionService loyaltySectionService;

    @PostMapping
    @PreAuthorize("hasAuthority('homepage.edit')")
    public ApiResponse<LoyaltySection> createOrUpdateLoyaltySection(@RequestBody LoyaltySectionRequest request) {
        String message = request != null && request.getId() != null
                ? "Loyalty section updated successfully."
                : "Loyalty section created successfully.";
        return ApiResponse.of(message, loyaltySectionService.createOrUpdateLoyaltySection(request));
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('homepage.view')")
    public List<LoyaltySection> getAllLoyaltySections() {
        return loyaltySectionService.getAllLoyaltySections();
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAuthority('homepage.view')")
    public LoyaltySection getLoyaltySection(@PathVariable Long id) {
        return loyaltySectionService.getLoyaltySection(id);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('homepage.edit')")
    public void deleteLoyaltySection(@PathVariable Long id) {
        loyaltySectionService.deleteLoyaltySection(id);
    }
}
