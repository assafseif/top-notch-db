package com.project.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HeroBannerRequest {
        private Long id;
    private List<String> imagesBase64;
    private String subtitle;
    private String title;
    private String description;
    private String cta;
    private String link;
}
