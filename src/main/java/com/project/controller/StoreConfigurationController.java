package com.project.controller;

import com.project.dto.ApiResponse;
import com.project.dto.StoreConfigurationDto;
import com.project.service.StoreConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuration")
public class StoreConfigurationController {

    @Autowired
    private StoreConfigurationService storeConfigurationService;

    @GetMapping
    public StoreConfigurationDto getCurrent() {
        return storeConfigurationService.getCurrent();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('configuration.edit')")
    public ApiResponse<StoreConfigurationDto> update(@RequestBody StoreConfigurationDto dto) {
        return ApiResponse.of("Configuration updated successfully.", storeConfigurationService.update(dto));
    }
}