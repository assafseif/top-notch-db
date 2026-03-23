package com.project.repository;

import com.project.entity.LimitedBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LimitedBannerRepository extends JpaRepository<LimitedBanner, Long> {
}
