package com.project.controller;

import com.project.dto.PermissionGroupDto;
import com.project.service.PermissionGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/permission-groups")
public class PermissionGroupController {
    @Autowired
    private PermissionGroupService permissionGroupService;

    @GetMapping
    public List<PermissionGroupDto> getAll() {
        return permissionGroupService.getAll();
    }

    @PostMapping
    public PermissionGroupDto create(@RequestBody PermissionGroupDto group) {
        return permissionGroupService.create(group);
    }

    @PutMapping("/{id}")
    public PermissionGroupDto update(@PathVariable Long id, @RequestBody PermissionGroupDto group) {
        return permissionGroupService.update(id, group);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        permissionGroupService.delete(id);
    }
}
