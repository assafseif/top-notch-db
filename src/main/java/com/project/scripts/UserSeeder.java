package com.project.scripts;

import com.project.entity.AppUser;
import com.project.entity.Role;
import com.project.repository.AppUserRepository;
import com.project.repository.RoleRepository;
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

    @Override
    @Transactional
    public void run(String... args) {
        if (appUserRepository.findByUsername("admin") == null) {
            Role adminRole = roleRepository.findByName("Super Admin");
            if (adminRole == null) {
                adminRole = Role.builder()
                        .name("Super Admin")
                        .description("Full access to everything")
                        .isSystem(true)
                        .build();
                adminRole = roleRepository.save(adminRole);
            }
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .fullName("Admin")
                    .email("admin@topnotch.com")
                    .role(adminRole)
                    .isActive(true)
                    .build();
            appUserRepository.save(admin);
        }
    }
}
