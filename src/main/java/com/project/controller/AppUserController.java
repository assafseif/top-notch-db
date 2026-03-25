package com.project.controller;

import com.project.dto.AppUserDto;
import com.project.entity.AppUser;
import com.project.repository.AppUserRepository;
import com.project.repository.RoleRepository;
import com.project.service.AppUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {
    @Autowired
    private AppUserService appUserService;

    // PATCH endpoint for partial update

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('users.edit')")
    public AppUserDto partialUpdate(@PathVariable Long id, @RequestBody AppUserDto user) {
        return appUserService.partialUpdate(id, user);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('users.view')")
    public List<AppUserDto> getAll() {
        return appUserService.getAll();
    }


    @GetMapping("/paged")
    @PreAuthorize("hasAuthority('users.view')")
    public Page<AppUserDto> getAllPaged(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return appUserService.getAllPaged(page, size);
    }


    @PostMapping
    @PreAuthorize("hasAuthority('users.create')")
    public AppUserDto create(@RequestBody AppUserDto user) {
        return appUserService.create(user);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users.edit')")
    public AppUserDto update(@PathVariable Long id, @RequestBody AppUserDto user) {
        return appUserService.update(id, user);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users.delete')")
    public void delete(@PathVariable Long id) {
        appUserService.delete(id);
    }
}
