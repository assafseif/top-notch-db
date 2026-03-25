package com.project.controller;

import com.project.dto.PermissionDto;
import com.project.entity.AppUser;
import com.project.entity.Role;
import com.project.service.AppUserService;
import com.project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/current-permissions")
public class CurrentUserPermissionController {
        @Autowired
        private com.project.repository.AppUserRepository appUserRepository;
    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<PermissionDto> getCurrentUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null || user.getRole() == null) return List.of();
        Role role = user.getRole();
        return role.getPermissions().stream()
                .map(p -> PermissionDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .label(p.getLabel())
                        .groupName(p.getGroupName())
                        .build())
                .collect(Collectors.toList());
    }
}
