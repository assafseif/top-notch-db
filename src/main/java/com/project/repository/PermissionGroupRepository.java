package com.project.repository;

import com.project.entity.PermissionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {
    PermissionGroup findByName(String name);
}
