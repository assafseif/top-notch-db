package com.project.service.impl;

import com.project.dto.PermissionDto;
import com.project.dto.RoleDto;
import com.project.entity.Permission;
import com.project.entity.Role;
import com.project.repository.AppUserRepository;
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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public List<RoleDto> getAll() {
        return roleRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<RoleDto> getAllPaged(PageRequest pageRequest) {
        Page<Role> page = roleRepository.findAll(pageRequest);
        List<RoleDto> dtos = page.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Override
    public RoleDto create(RoleDto dto) {
        validateRole(dto, null);
        return toDto(roleRepository.save(toEntity(dto)));
    }

    @Override
    public RoleDto update(Long id, RoleDto dto) {
        validateRole(dto, id);

        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found."));

        Role role = toEntity(dto);
        role.setId(id);
        role.setSystem(existing.isSystem());
        return toDto(roleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role not found."));
        if (role.isSystem()) {
            throw new IllegalStateException("System roles cannot be deleted.");
        }
        if (appUserRepository.countByRole_Id(id) > 0) {
            throw new IllegalStateException("Cannot delete role because it is assigned to existing users.");
        }

        roleRepository.deleteById(id);
    }

    private void validateRole(RoleDto dto, Long currentRoleId) {
        if (dto == null) {
            throw new IllegalArgumentException("Role details are required.");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name is required.");
        }

        Role existing = roleRepository.findByName(dto.getName().trim());
        if (existing != null && (currentRoleId == null || !existing.getId().equals(currentRoleId))) {
            throw new IllegalArgumentException("Role name already exists.");
        }
    }

    private RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }
        List<PermissionDto> permissions = role.getPermissions() == null
                ? null
                : role.getPermissions().stream()
                .map(permission -> PermissionDto.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .label(permission.getLabel())
                        .groupName(permission.getGroupName())
                        .build())
                .collect(Collectors.toList());
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions)
                .system(role.isSystem())
                .build();
    }

    private Role toEntity(RoleDto dto) {
        if (dto == null) {
            return null;
        }
        Role role = Role.builder()
                .id(dto.getId())
                .name(dto.getName() == null ? null : dto.getName().trim())
                .description(dto.getDescription())
                .isSystem(dto.isSystem())
                .build();
        if (dto.getPermissionIds() != null) {
            List<Permission> permissions = dto.getPermissionIds().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            role.setPermissions(new HashSet<>(permissions));
        } else if (dto.getPermissions() != null) {
            List<Permission> permissions = dto.getPermissions().stream()
                    .map(permission -> Permission.builder()
                            .id(permission.getId())
                            .name(permission.getName())
                            .label(permission.getLabel())
                            .groupName(permission.getGroupName())
                            .build())
                    .collect(Collectors.toList());
            role.setPermissions(new HashSet<>(permissions));
        }
        return role;
    }
}
