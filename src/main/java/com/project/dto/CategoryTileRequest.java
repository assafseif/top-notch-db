package com.project.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryTileRequest {
        private Long id;
    private String name;
    private String imageBase64;
    private String link;
}
