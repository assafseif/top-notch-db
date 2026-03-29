package com.project.controller;

import com.project.dto.AppUserDto;
import com.project.dto.ApiResponse;
import com.project.dto.UserPasswordUpdateRequest;
import com.project.service.AppUserService;
import com.project.service.StoreConfigurationService;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {
    @Autowired
    private AppUserService appUserService;

    @Autowired
    private StoreConfigurationService storeConfigurationService;

    // PATCH endpoint for partial update

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('users.edit')")
    public ApiResponse<AppUserDto> partialUpdate(@PathVariable Long id, @RequestBody AppUserDto user) {
        return ApiResponse.of("User updated successfully.", appUserService.partialUpdate(id, user));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('users.change_password')")
    public ApiResponse<AppUserDto> updatePassword(@PathVariable Long id, @RequestBody UserPasswordUpdateRequest request) {
        return ApiResponse.of("Password updated successfully.", appUserService.updatePassword(id, request.getPassword()));
    }


    @GetMapping
    @PreAuthorize("hasAuthority('users.view')")
    public List<AppUserDto> getAll() {
        return appUserService.getAll();
    }


    @GetMapping("/paged")
    @PreAuthorize("hasAuthority('users.view')")
    public Page<AppUserDto> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return appUserService.getAllPaged(page, storeConfigurationService.resolvePageSize(size));
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
