package com.project.repository;

import com.project.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long>, JpaSpecificationExecutor<CustomerOrder> {
    Optional<CustomerOrder> findByPublicId(String publicId);

    boolean existsByPublicId(String publicId);
}