package com.project.dto;


import java.util.List;

import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PermissionGroupDto {
    private List<PermissionDto> permissions;
    private String label;
    private String name;
    private Long id;
}


