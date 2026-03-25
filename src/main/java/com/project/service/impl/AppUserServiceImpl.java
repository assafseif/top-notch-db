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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {
    private static final int MIN_PASSWORD_LENGTH = 6;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public AppUserDto create(AppUserDto userDto) {
        validateCreate(userDto);

        AppUser user = new AppUser();
        user.setCreatedAt(new Date());
        applyUserChanges(user, userDto, true, true);

        return toDto(appUserRepository.save(user));
    }

    @Override
    public AppUserDto update(Long id, AppUserDto userDto) {
        validateUpdate(id, userDto);

        AppUser existing = findExistingUser(id);
        applyUserChanges(existing, userDto, true, false);

        return toDto(appUserRepository.save(existing));
    }

    @Override
    public AppUserDto partialUpdate(Long id, AppUserDto userDto) {
        validatePartialUpdate(id, userDto);

        AppUser existing = findExistingUser(id);
        if (!applyUserChanges(existing, userDto, false, false)) {
            throw new IllegalArgumentException("No changes were submitted.");
        }

        return toDto(appUserRepository.save(existing));
    }

    @Override
    public void delete(Long id) {
        AppUser existing = findExistingUser(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            if (currentUsername != null && currentUsername.equals(existing.getUsername())) {
                throw new IllegalStateException("You cannot delete your own account.");
            }
        }

        appUserRepository.deleteById(id);
    }

    @Override
    public List<AppUserDto> getAll() {
        return appUserRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<AppUserDto> getAllPaged(int page, int size) {
        Page<AppUser> pagedUsers = appUserRepository.findAll(PageRequest.of(page, size));
        List<AppUserDto> dtos = pagedUsers.stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<>(dtos, pagedUsers.getPageable(), pagedUsers.getTotalElements());
    }

    @Override
    public AppUserDto getById(Long id) {
        return toDto(findExistingUser(id));
    }

    private void validateCreate(AppUserDto userDto) {
        requirePayload(userDto);
        requireText(userDto.getUsername(), "Username is required.");
        requireText(userDto.getFullName(), "Full name is required.");
        requireRole(userDto.getRoleId());
        validatePassword(userDto.getPassword(), true);
        validateDuplicateUsername(userDto.getUsername(), null);
    }

    private void validateUpdate(Long id, AppUserDto userDto) {
        requirePayload(userDto);
        requireText(userDto.getUsername(), "Username is required.");
        requireText(userDto.getFullName(), "Full name is required.");
        requireRole(userDto.getRoleId());
        validatePassword(userDto.getPassword(), false);
        validateDuplicateUsername(userDto.getUsername(), id);
    }

    private void validatePartialUpdate(Long id, AppUserDto userDto) {
        requirePayload(userDto);
        if (userDto.getUsername() != null) {
            requireText(userDto.getUsername(), "Username is required.");
            validateDuplicateUsername(userDto.getUsername(), id);
        }
        if (userDto.getFullName() != null) {
            requireText(userDto.getFullName(), "Full name is required.");
        }
        if (userDto.getRoleId() != null) {
            requireRole(userDto.getRoleId());
        }
        validatePassword(userDto.getPassword(), false);
    }

    private boolean applyUserChanges(AppUser user, AppUserDto userDto, boolean fullReplace, boolean creating) {
        boolean changed = false;

        if (fullReplace || userDto.getUsername() != null) {
            String username = normalize(userDto.getUsername());
            if (!Objects.equals(user.getUsername(), username)) {
                user.setUsername(username);
                changed = true;
            }
        }

        if (fullReplace || userDto.getFullName() != null) {
            String fullName = normalize(userDto.getFullName());
            if (!Objects.equals(user.getFullName(), fullName)) {
                user.setFullName(fullName);
                changed = true;
            }
        }

        if (fullReplace || userDto.getEmail() != null) {
            String email = normalizeNullable(userDto.getEmail());
            if (!Objects.equals(user.getEmail(), email)) {
                user.setEmail(email);
                changed = true;
            }
        }

        if (fullReplace || userDto.getRoleId() != null) {
            Role role = userDto.getRoleId() == null ? null : resolveRole(userDto.getRoleId());
            Long currentRoleId = user.getRole() != null ? user.getRole().getId() : null;
            Long incomingRoleId = role != null ? role.getId() : null;
            if (!Objects.equals(currentRoleId, incomingRoleId)) {
                user.setRole(role);
                changed = true;
            }
        }

        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            String encodedPassword = userDto.getPassword().startsWith("$2a$")
                    ? userDto.getPassword()
                    : passwordEncoder.encode(userDto.getPassword());
            if (!Objects.equals(user.getPassword(), encodedPassword)) {
                user.setPassword(encodedPassword);
                changed = true;
            }
        }

        if (fullReplace) {
            boolean nextActive = userDto.getActive() == null || userDto.getActive();
            if (creating || user.isActive() != nextActive) {
                user.setActive(nextActive);
                changed = true;
            }
        } else if (userDto.getActive() != null && user.isActive() != userDto.getActive()) {
            user.setActive(userDto.getActive());
            changed = true;
        }

        if (creating && user.getCreatedAt() == null) {
            user.setCreatedAt(new Date());
            changed = true;
        }

        return changed;
    }

    private AppUser findExistingUser(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
    }

    private void validateDuplicateUsername(String username, Long currentUserId) {
        AppUser existingUser = appUserRepository.findByUsername(normalize(username));
        if (existingUser != null && (currentUserId == null || !existingUser.getId().equals(currentUserId))) {
            throw new IllegalArgumentException("Username already exists.");
        }
    }

    private void validatePassword(String password, boolean required) {
        if (password == null || password.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("Password is required.");
            }
            return;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private Role resolveRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Selected role does not exist."));
    }

    private void requireRole(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role is required.");
        }
        resolveRole(roleId);
    }

    private void requirePayload(AppUserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("User details are required.");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }

    private AppUserDto toDto(AppUser user) {
        if (user == null) {
            return null;
        }

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
        return dto;
    }
}

