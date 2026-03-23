package com.project.service;

import com.project.dto.LoyaltySectionRequest;
import com.project.entity.LoyaltySection;
import java.util.List;

public interface LoyaltySectionService {
    LoyaltySection createOrUpdateLoyaltySection(LoyaltySectionRequest request);
    List<LoyaltySection> getAllLoyaltySections();
    LoyaltySection getLoyaltySection(Long id);
    void deleteLoyaltySection(Long id);
}
