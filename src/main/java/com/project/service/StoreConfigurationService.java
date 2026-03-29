package com.project.service;

import com.project.dto.StoreConfigurationDto;

public interface StoreConfigurationService {
    StoreConfigurationDto getCurrent();
    StoreConfigurationDto update(StoreConfigurationDto dto);
    int resolvePageSize(Integer requestedSize);
}