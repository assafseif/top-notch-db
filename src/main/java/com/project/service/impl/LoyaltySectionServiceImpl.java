package com.project.service.impl;

import com.project.dto.LoyaltySectionRequest;
import com.project.dto.LoyaltyPerkRequest;
import com.project.entity.LoyaltySection;
import com.project.entity.LoyaltyPerk;
import com.project.repository.LoyaltySectionRepository;
import com.project.service.LoyaltySectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoyaltySectionServiceImpl implements LoyaltySectionService {
    @Autowired
    private LoyaltySectionRepository loyaltySectionRepository;

    @Override
    public LoyaltySection createOrUpdateLoyaltySection(LoyaltySectionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Loyalty section details are required.");
        }
        if (request.getHeading() == null || request.getHeading().trim().isEmpty()) {
            throw new IllegalArgumentException("Loyalty section heading is required.");
        }

        LoyaltySection section = request.getId() != null
                ? loyaltySectionRepository.findById(request.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Loyalty section not found."))
                : new LoyaltySection();

        section.setHeading(request.getHeading());
        section.setButtonText(request.getButtonText());
        section.setDescription(request.getDescription());
        if (request.getPerks() != null) {
            List<LoyaltyPerk> perks = request.getPerks().stream().map(p -> {
                LoyaltyPerk perk = new LoyaltyPerk();
                perk.setIcon(p.getIcon());
                perk.setTitle(p.getTitle());
                perk.setDescription(p.getDescription());
                return perk;
            }).collect(Collectors.toList());
            section.setPerks(perks);
        }
        return loyaltySectionRepository.save(section);
    }

    @Override
    public List<LoyaltySection> getAllLoyaltySections() {
        return loyaltySectionRepository.findAll();
    }

    @Override
    public LoyaltySection getLoyaltySection(Long id) {
        return loyaltySectionRepository.findById(id).orElse(null);
    }
    @Override
    public void deleteLoyaltySection(Long id) {
        loyaltySectionRepository.deleteById(id);
    }
}
