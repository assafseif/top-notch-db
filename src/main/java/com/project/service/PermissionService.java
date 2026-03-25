package com.project.service;


import java.util.List;
import com.project.dto.PermissionDto;
public interface PermissionService {
    void delete(Long id);
    PermissionDto update(Long id, PermissionDto dto);
    PermissionDto create(PermissionDto dto);
    List<PermissionDto> getAll();
}



