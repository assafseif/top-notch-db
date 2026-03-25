package com.project.service.impl;

import com.project.dto.AppUserDto;
import com.project.entity.AppUser;
import com.project.entity.Role;
import com.project.repository.AppUserRepository;
import com.project.repository.RoleRepository;
import com.project.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AppUserDto create(AppUserDto userDto) {
        AppUser user = toEntity(userDto);
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() != null && user.getRole().getId() != null) {
            user.setRole(roleRepository.findById(user.getRole().getId()).orElse(null));
        }
        AppUser saved = appUserRepository.save(user);
        return toDto(saved);
    }

    @Override
    public AppUserDto update(Long id, AppUserDto userDto) {
        AppUser user = toEntity(userDto);
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() != null && user.getRole().getId() != null) {
            user.setRole(roleRepository.findById(user.getRole().getId()).orElse(null));
        }
        AppUser saved = appUserRepository.save(user);
        return toDto(saved);
    }

    @Override
    public AppUserDto partialUpdate(Long id, AppUserDto userDto) {
        AppUser existing = appUserRepository.findById(id).orElseThrow();
        if (userDto.getUsername() != null) existing.setUsername(userDto.getUsername());
        if (userDto.getFullName() != null) existing.setFullName(userDto.getFullName());
        if (userDto.getEmail() != null) existing.setEmail(userDto.getEmail());
        if (userDto.getRoleId() != null) {
            existing.setRole(roleRepository.findById(userDto.getRoleId()).orElse(null));
        }
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            if (!userDto.getPassword().startsWith("$2a$")) {
                existing.setPassword(passwordEncoder.encode(userDto.getPassword()));
            } else {
                existing.setPassword(userDto.getPassword());
            }
        }
        existing.setActive(userDto.isActive());
        AppUser saved = appUserRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        appUserRepository.deleteById(id);
    }

    @Override
    public List<AppUserDto> getAll() {
        return appUserRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<AppUserDto> getAllPaged(int page, int size) {
        Page<AppUser> p = appUserRepository.findAll(PageRequest.of(page, size));
        List<AppUserDto> dtos = p.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, p.getPageable(), p.getTotalElements());
    }

    @Override
    public AppUserDto getById(Long id) {
        return toDto(appUserRepository.findById(id).orElseThrow());
    }

    private AppUserDto toDto(AppUser user) {
        if (user == null) return null;
        AppUserDto dto = AppUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getName());
        }
        // don't set password on DTO for reading
        return dto;
    }

    private AppUser toEntity(AppUserDto dto) {
        if (dto == null) return null;
        AppUser.AppUserBuilder builder = AppUser.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .isActive(dto.isActive());
        if (dto.getCreatedAt() != null) {
            builder.createdAt(dto.getCreatedAt());
        }
        AppUser user = builder.build();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(new Date());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (!dto.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            } else {
                user.setPassword(dto.getPassword());
            }
        }
        if (dto.getRoleId() != null) {
            Role r = new Role();
            r.setId(dto.getRoleId());
            user.setRole(r);
        }
        return user;
    }
}

