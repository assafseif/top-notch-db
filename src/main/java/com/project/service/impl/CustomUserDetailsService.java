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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username);
        // Force initialization of permissions to avoid LazyInitializationException
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().size();
        }
        List<GrantedAuthority> authorities = user.getRole().getPermissions().stream()
            .map(p -> new SimpleGrantedAuthority(p.getName()))
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), user.getPassword(), user.isActive(), true, true, true, authorities);
    }
}
