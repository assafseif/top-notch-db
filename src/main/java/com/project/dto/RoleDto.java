package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDto {
    private Long id;
    private String name;
    private String description;
    private List<PermissionDto> permissions; // for output
    private List<Long> permissionIds; // for input
    private boolean system;
}

