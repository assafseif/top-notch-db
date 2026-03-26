package com.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lockoutEndsAt;

    @Column(nullable = false)
    @Builder.Default
    private Date createdAt = new Date();
}
