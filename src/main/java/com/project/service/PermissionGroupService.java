package com.project.service;

import com.project.dto.PermissionGroupDto;
import java.util.List;

public interface PermissionGroupService {
    List<PermissionGroupDto> getAll();
    PermissionGroupDto create(PermissionGroupDto dto);
    PermissionGroupDto update(Long id, PermissionGroupDto dto);
    void delete(Long id);
}

