package com.project.controller;

import com.project.dto.LoyaltySectionRequest;
import com.project.entity.LoyaltySection;
import com.project.service.LoyaltySectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/loyalty-section")
public class LoyaltySectionController {
    @Autowired
    private LoyaltySectionService loyaltySectionService;

    @PostMapping
    public LoyaltySection createOrUpdateLoyaltySection(@RequestBody LoyaltySectionRequest request) {
        return loyaltySectionService.createOrUpdateLoyaltySection(request);
    }

    @GetMapping
    public List<LoyaltySection> getAllLoyaltySections() {
        return loyaltySectionService.getAllLoyaltySections();
    }

    @GetMapping("/{id}")
    public LoyaltySection getLoyaltySection(@PathVariable Long id) {
        return loyaltySectionService.getLoyaltySection(id);
    }
    @DeleteMapping("/{id}")
    public void deleteLoyaltySection(@PathVariable Long id) {
        loyaltySectionService.deleteLoyaltySection(id);
    }
}
