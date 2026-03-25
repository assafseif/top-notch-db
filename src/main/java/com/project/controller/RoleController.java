package com.project.controller;

import com.project.dto.RoleDto;
import com.project.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;


    @GetMapping
    public List<RoleDto> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/paged")
    public Page<RoleDto> getAllPaged(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return roleService.getAllPaged(PageRequest.of(page, size));
    }

    @PostMapping
    public RoleDto create(@RequestBody RoleDto role) {
        return roleService.create(role);
    }

    @PutMapping("/{id}")
    public RoleDto update(@PathVariable Long id, @RequestBody RoleDto role) {
        return roleService.update(id, role);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
