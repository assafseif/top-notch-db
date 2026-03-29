package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.PermissionDto;
import com.project.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public List<PermissionDto> getAll() {
        return permissionService.getAll();
    }

    @PostMapping
    public ApiResponse<PermissionDto> create(@RequestBody PermissionDto permission) {
        return ApiResponse.of("Permission created successfully.", permissionService.create(permission));
    }

    @PutMapping("/{id}")
    public ApiResponse<PermissionDto> update(@PathVariable Long id, @RequestBody PermissionDto permission) {
        return ApiResponse.of("Permission updated successfully.", permissionService.update(id, permission));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ApiResponse.of("Permission deleted successfully.");
    }
}
