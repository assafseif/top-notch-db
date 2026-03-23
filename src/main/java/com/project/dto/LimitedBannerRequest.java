package com.project.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LimitedBannerRequest {
    private Long id;
    private String imageBase64;
    private String badge;
    private String title;
    private String description;
    private String cta;
    private String link;
}
