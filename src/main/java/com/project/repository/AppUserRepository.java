package com.project.repository;

import com.project.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    AppUser findByUsername(@Param("username") String username);

    AppUser findByEmail(String email);
}
