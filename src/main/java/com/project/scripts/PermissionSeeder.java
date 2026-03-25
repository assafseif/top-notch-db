package com.project.scripts;

import com.project.entity.Permission;
import com.project.entity.PermissionGroup;
import com.project.repository.PermissionGroupRepository;
import com.project.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class PermissionSeeder implements CommandLineRunner {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Define permission groups and permissions (from frontend PERMISSION_GROUPS)
        List<PermissionGroupSeed> groups = List.of(
            new PermissionGroupSeed("products", "Products", List.of(
                new PermissionSeed("products.view", "View Products"),
                new PermissionSeed("products.create", "Create Products"),
                new PermissionSeed("products.edit", "Edit Products"),
                new PermissionSeed("products.delete", "Delete Products")
            )),
            new PermissionGroupSeed("homepage", "Homepage", List.of(
                new PermissionSeed("homepage.view", "View Homepage Settings"),
                new PermissionSeed("homepage.edit", "Edit Homepage Content")
            )),
            new PermissionGroupSeed("users", "User Management", List.of(
                new PermissionSeed("users.view", "View Users"),
                new PermissionSeed("users.create", "Create Users"),
                new PermissionSeed("users.edit", "Edit Users"),
                new PermissionSeed("users.delete", "Delete Users"),
                new PermissionSeed("users.change_password", "Change User Passwords")
            )),
            new PermissionGroupSeed("roles", "Role Management", List.of(
                new PermissionSeed("roles.view", "View Roles"),
                new PermissionSeed("roles.create", "Create Roles"),
                new PermissionSeed("roles.edit", "Edit Roles"),
                new PermissionSeed("roles.delete", "Delete Roles")
            )),
            new PermissionGroupSeed("orders", "Orders", List.of(
                new PermissionSeed("orders.view", "View Orders"),
                new PermissionSeed("orders.manage", "Manage Orders")
            )),
            new PermissionGroupSeed("wholesale", "Wholesale", List.of(
                new PermissionSeed("wholesale.view", "View Wholesale Inquiries"),
                new PermissionSeed("wholesale.manage", "Manage Wholesale")
            ))
        );

        for (PermissionGroupSeed groupSeed : groups) {
            PermissionGroup group = permissionGroupRepository.findByName(groupSeed.name);
            if (group == null) {
                group = PermissionGroup.builder()
                        .name(groupSeed.name)
                        .label(groupSeed.label)
                        .build();
                group = permissionGroupRepository.save(group);
            }
            // ensure permissions list is initialized
            if (group.getPermissions() == null) {
                group.setPermissions(new ArrayList<>());
            }

            for (PermissionSeed permSeed : groupSeed.permissions) {
                Permission perm = permissionRepository.findByName(permSeed.id);
                if (perm == null) {
                    perm = Permission.builder()
                            .name(permSeed.id)
                            .label(permSeed.label)
                            .groupName(groupSeed.name)
                            .build();
                    perm = permissionRepository.save(perm);
                }
                // capture id in an effectively-final variable for use in lambda
                Long permId = perm.getId();
                // add only if not already present to avoid duplicates
                boolean exists = group.getPermissions().stream().anyMatch(p -> Objects.equals(p.getId(), permId));
                if (!exists) {
                    group.getPermissions().add(perm);
                }
            }
            permissionGroupRepository.save(group);
        }
    }

    private static class PermissionGroupSeed {
        String name;
        String label;
        List<PermissionSeed> permissions;
        PermissionGroupSeed(String name, String label, List<PermissionSeed> permissions) {
            this.name = name;
            this.label = label;
            this.permissions = permissions;
        }
    }
    private static class PermissionSeed {
        String id;
        String label;
        PermissionSeed(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
}
