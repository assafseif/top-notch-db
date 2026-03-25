package com.project.scripts;

import com.project.entity.AppUser;
import com.project.entity.Role;
import com.project.repository.AppUserRepository;
import com.project.repository.RoleRepository;
import com.project.repository.PermissionRepository;
import com.project.entity.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserSeeder implements CommandLineRunner {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {

    }
}
