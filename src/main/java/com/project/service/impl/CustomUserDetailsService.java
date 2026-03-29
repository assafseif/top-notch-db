package com.project.service.impl;

import com.project.entity.AppUser;
import com.project.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }

        // Force initialization of permissions to avoid LazyInitializationException
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().size();
        }
        List<GrantedAuthority> authorities = user.getRole().getPermissions().stream()
            .map(p -> new SimpleGrantedAuthority(p.getName()))
            .collect(Collectors.toList());

        boolean accountNonLocked = user.getLockoutEndsAt() == null || !user.getLockoutEndsAt().after(new Date());

        String encodedPassword = user.getPassword() == null ? "" : user.getPassword();

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), encodedPassword, user.isActive(), true, true, accountNonLocked, authorities);
    }
}
