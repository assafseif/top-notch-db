package com.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoyaltyPerkRequest {
    private String icon;
    private String title;
    private String description;
}
