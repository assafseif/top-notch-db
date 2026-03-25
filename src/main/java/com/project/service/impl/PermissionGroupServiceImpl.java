package com.project.service.impl;

import com.project.dto.PermissionDto;
import com.project.dto.PermissionGroupDto;
import com.project.entity.Permission;
import com.project.entity.PermissionGroup;
import com.project.repository.PermissionGroupRepository;
import com.project.repository.PermissionRepository;
import com.project.service.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionGroupServiceImpl implements PermissionGroupService {
    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<PermissionGroupDto> getAll() {
        return permissionGroupRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PermissionGroupDto create(PermissionGroupDto dto) {
        PermissionGroup g = toEntity(dto);
        if (g.getPermissions() != null) {
            // ensure permissions have ids or are saved before linking
        }
        PermissionGroup saved = permissionGroupRepository.save(g);
        return toDto(saved);
    }

    @Override
    public PermissionGroupDto update(Long id, PermissionGroupDto dto) {
        PermissionGroup g = toEntity(dto);
        g.setId(id);
        PermissionGroup saved = permissionGroupRepository.save(g);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        permissionGroupRepository.deleteById(id);
    }

    private PermissionGroupDto toDto(PermissionGroup g) {
        if (g == null) return null;
        List<PermissionDto> perms = g.getPermissions() == null ? null : g.getPermissions().stream().map(this::permToDto).collect(Collectors.toList());
        return PermissionGroupDto.builder()
                .id(g.getId())
                .name(g.getName())
                .label(g.getLabel())
                .permissions(perms)
                .build();
    }

    private PermissionGroup toEntity(PermissionGroupDto dto) {
        if (dto == null) return null;
        PermissionGroup g = PermissionGroup.builder()
                .id(dto.getId())
                .name(dto.getName())
                .label(dto.getLabel())
                .build();
        if (dto.getPermissions() != null) {
            List<Permission> perms = dto.getPermissions().stream().map(d -> {
                Permission p = Permission.builder().id(d.getId()).name(d.getName()).label(d.getLabel()).groupName(d.getGroupName()).build();
                return p;
            }).collect(Collectors.toList());
            g.setPermissions(perms);
        }
        return g;
    }

    private PermissionDto permToDto(Permission p) {
        if (p == null) return null;
        return PermissionDto.builder().id(p.getId()).name(p.getName()).label(p.getLabel()).groupName(p.getGroupName()).build();
    }
}

