package com.project.service.impl;

import com.project.dto.PermissionDto;
import com.project.dto.RoleDto;
import com.project.entity.Permission;
import com.project.entity.Role;
import com.project.repository.PermissionRepository;
import com.project.repository.RoleRepository;
import com.project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<RoleDto> getAll() {
        return roleRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<RoleDto> getAllPaged(PageRequest pageRequest) {
        Page<Role> p = roleRepository.findAll(pageRequest);
        List<RoleDto> dtos = p.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, p.getPageable(), p.getTotalElements());
    }

    @Override
    public RoleDto create(RoleDto dto) {
        Role r = toEntity(dto);
        Role saved = roleRepository.save(r);
        return toDto(saved);
    }

    @Override
    public RoleDto update(Long id, RoleDto dto) {
        Role r = toEntity(dto);
        r.setId(id);
        Role saved = roleRepository.save(r);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    private RoleDto toDto(Role r) {
        if (r == null) return null;
        List<PermissionDto> perms = r.getPermissions() == null ? null : r.getPermissions().stream().map(p -> PermissionDto.builder().id(p.getId()).name(p.getName()).label(p.getLabel()).groupName(p.getGroupName()).build()).collect(Collectors.toList());
        return RoleDto.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .permissions(perms)
                .system(r.isSystem())
                .build();
    }

    private Role toEntity(RoleDto dto) {
        if (dto == null) return null;
        Role r = Role.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .isSystem(dto.isSystem())
                .build();
        if (dto.getPermissionIds() != null) {
            List<Permission> perms = dto.getPermissionIds().stream()
                .map(pid -> permissionRepository.findById(pid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            r.setPermissions(new HashSet<>(perms));
        } else if (dto.getPermissions() != null) {
            List<Permission> perms = dto.getPermissions().stream().map(d -> Permission.builder().id(d.getId()).name(d.getName()).label(d.getLabel()).groupName(d.getGroupName()).build()).collect(Collectors.toList());
            r.setPermissions(new HashSet<>(perms));
        }
        return r;
    }
}
