package com.project.service.impl;

import com.project.dto.StoreConfigurationDto;
import com.project.entity.StoreConfiguration;
import com.project.repository.StoreConfigurationRepository;
import com.project.service.StoreConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreConfigurationServiceImpl implements StoreConfigurationService {
    private static final String DEFAULT_WHATSAPP_NUMBER = "+96170238705";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String WHATSAPP_NUMBER_FIELD = "whatsapp_number";
    private static final String PAGINATION_FIELD = "pagination";

    @Autowired
    private StoreConfigurationRepository storeConfigurationRepository;

    @Override
    public StoreConfigurationDto getCurrent() {
        return StoreConfigurationDto.builder()
                .whatsappNumber(getConfigurationValue(WHATSAPP_NUMBER_FIELD, DEFAULT_WHATSAPP_NUMBER))
                .defaultPageSize(resolveConfiguredPageSize())
                .build();
    }

    @Override
    public StoreConfigurationDto update(StoreConfigurationDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Configuration details are required.");
        }

        String whatsappNumber = dto.getWhatsappNumber() == null ? "" : dto.getWhatsappNumber().trim();
        if (whatsappNumber.isEmpty()) {
            throw new IllegalArgumentException("WhatsApp number is required.");
        }

        Integer defaultPageSize = dto.getDefaultPageSize();
        if (defaultPageSize == null || defaultPageSize < 1) {
            throw new IllegalArgumentException("Default page size must be at least 1.");
        }

        upsertConfigurationValue(WHATSAPP_NUMBER_FIELD, whatsappNumber);
        upsertConfigurationValue(PAGINATION_FIELD, String.valueOf(defaultPageSize));
        return getCurrent();
    }

    @Override
    public int resolvePageSize(Integer requestedSize) {
        if (requestedSize != null && requestedSize > 0) {
            return requestedSize;
        }

        return resolveConfiguredPageSize();
    }

    private int resolveConfiguredPageSize() {
        String configuredValue = getConfigurationValue(PAGINATION_FIELD, String.valueOf(DEFAULT_PAGE_SIZE));
        try {
            int pageSize = Integer.parseInt(configuredValue);
            return pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        } catch (NumberFormatException exception) {
            return DEFAULT_PAGE_SIZE;
        }
    }

    private String getConfigurationValue(String fieldName, String defaultValue) {
        return storeConfigurationRepository.findByFieldName(fieldName)
                .map(StoreConfiguration::getFieldValue)
                .filter(value -> value != null && !value.trim().isEmpty())
                .orElseGet(() -> upsertConfigurationValue(fieldName, defaultValue).getFieldValue());
    }

    private StoreConfiguration upsertConfigurationValue(String fieldName, String fieldValue) {
        StoreConfiguration configuration = storeConfigurationRepository.findByFieldName(fieldName)
                .orElseGet(() -> StoreConfiguration.builder().fieldName(fieldName).build());
        configuration.setFieldValue(fieldValue);
        return storeConfigurationRepository.save(configuration);
    }
}