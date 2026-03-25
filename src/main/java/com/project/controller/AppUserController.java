package com.project.controller;

import com.project.dto.AppUserDto;
import com.project.dto.ApiResponse;
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
    public ApiResponse<AppUserDto> partialUpdate(@PathVariable Long id, @RequestBody AppUserDto user) {
        AppUserDto updatedUser = appUserService.partialUpdate(id, user);
        boolean passwordOnlyUpdate = user.getPassword() != null
                && !user.getPassword().isBlank()
                && user.getUsername() == null
                && user.getFullName() == null
                && user.getEmail() == null
                && user.getRoleId() == null
                && user.getActive() == null;

        return ApiResponse.of(
                passwordOnlyUpdate ? "Password updated successfully." : "User updated successfully.",
                updatedUser
        );
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
    public ApiResponse<AppUserDto> create(@RequestBody AppUserDto user) {
        return ApiResponse.of("User created successfully.", appUserService.create(user));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users.edit')")
    public ApiResponse<AppUserDto> update(@PathVariable Long id, @RequestBody AppUserDto user) {
        return ApiResponse.of("User updated successfully.", appUserService.update(id, user));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users.delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        appUserService.delete(id);
        return ApiResponse.of("User deleted successfully.");
    }
}
