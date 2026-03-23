package com.project.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoyaltySectionRequest {
        private Long id;
    private String heading;
    private String buttonText;
    private String description;
    private List<LoyaltyPerkRequest> perks;
}
