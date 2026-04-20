package com.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SubcategoryRequest {
    private String name;
    private Long categoryId;
}