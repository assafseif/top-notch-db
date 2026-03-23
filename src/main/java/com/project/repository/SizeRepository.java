package com.project.repository;

import com.project.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Long> {
    Optional<Size> findByValue(String value);
}

