package com.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "permission_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String label;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();
}
