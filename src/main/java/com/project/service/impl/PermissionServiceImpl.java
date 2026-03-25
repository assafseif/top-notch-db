package com.project.service.impl;

import com.project.dto.PermissionDto;
import com.project.entity.Permission;
import com.project.repository.PermissionRepository;
import com.project.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<PermissionDto> getAll() {
        return permissionRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PermissionDto create(PermissionDto dto) {
        Permission p = toEntity(dto);
        Permission saved = permissionRepository.save(p);
        return toDto(saved);
    }

    @Override
    public PermissionDto update(Long id, PermissionDto dto) {
        Permission p = toEntity(dto);
        p.setId(id);
        Permission saved = permissionRepository.save(p);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        permissionRepository.deleteById(id);
    }

    private PermissionDto toDto(Permission p) {
        if (p == null) return null;
        return PermissionDto.builder()
                .id(p.getId())
                .name(p.getName())
                .label(p.getLabel())
                .groupName(p.getGroupName())
                .build();
    }

    private Permission toEntity(PermissionDto dto) {
        if (dto == null) return null;
        Permission p = Permission.builder()
                .id(dto.getId())
                .name(dto.getName())
                .label(dto.getLabel())
                .groupName(dto.getGroupName())
                .build();
        return p;
    }
}

