package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.RoleDto;
import com.project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // ✅ Get all roles
    @GetMapping
    @PreAuthorize("hasAuthority('roles.view')")
    public List<RoleDto> getAll() {
        return roleService.getAll();
    }

    // ✅ Get paginated roles
    @GetMapping("/paged")
    @PreAuthorize("hasAuthority('roles.view')")
    public Page<RoleDto> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return roleService.getAllPaged(PageRequest.of(page, size));
    }

    // ✅ Create role
    @PostMapping
    @PreAuthorize("hasAuthority('roles.create')")
    public ApiResponse<RoleDto> create(@RequestBody RoleDto role) {
        return ApiResponse.of("Role created successfully.", roleService.create(role));
    }

    // ✅ Update role
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.edit')")
    public ApiResponse<RoleDto> update(@PathVariable Long id, @RequestBody RoleDto role) {
        return ApiResponse.of("Role updated successfully.", roleService.update(id, role));
    }

    // ✅ Delete role
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('roles.delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.of("Role deleted successfully.");
    }
}