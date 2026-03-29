package com.project.repository;

import com.project.entity.StoreConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreConfigurationRepository extends JpaRepository<StoreConfiguration, Long> {
    Optional<StoreConfiguration> findByFieldName(String fieldName);
}