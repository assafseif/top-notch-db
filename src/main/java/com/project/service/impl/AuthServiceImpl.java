package com.project.service.impl;

import com.project.dto.AppUserDto;
import com.project.dto.LoginResponse;
import com.project.entity.AppUser;
import com.project.repository.AppUserRepository;
import com.project.security.JwtTokenProvider;
import com.project.service.AuthService;
import com.project.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid username or password.";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserService appUserService;

    @Value("${app.security.auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.auth.lockout-minutes:15}")
    private long lockoutMinutes;

    @Override
    public LoginResponse login(Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        AppUser user = appUserRepository.findByUsername(username.trim());
        if (user == null) {
            throw new IllegalArgumentException(INVALID_CREDENTIALS_MESSAGE);
        }

        if (!user.isActive()) {
            throw new IllegalStateException("Your account is inactive. Please contact an administrator.");
        }

        ensureUserIsNotLocked(user);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username.trim(), password));
        } catch (BadCredentialsException ex) {
            throw handleFailedLogin(user);
        }

        resetFailedAttempts(user);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        AppUserDto dto = appUserService.getById(user.getId());
        return new LoginResponse(token, dto);
    }

    private void ensureUserIsNotLocked(AppUser user) {
        Date now = new Date();
        Date lockoutEndsAt = user.getLockoutEndsAt();

        if (lockoutEndsAt == null) {
            return;
        }

        if (lockoutEndsAt.after(now)) {
            long remainingMillis = lockoutEndsAt.getTime() - now.getTime();
            long remainingMinutes = Math.max(1, TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
            throw new IllegalStateException("Too many failed login attempts. Try again in " + remainingMinutes + " minute(s).");
        }

        user.setLockoutEndsAt(null);
        user.setFailedLoginAttempts(0);
        appUserRepository.save(user);
    }

    private RuntimeException handleFailedLogin(AppUser user) {
        int nextFailedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(nextFailedAttempts);

        if (nextFailedAttempts >= maxFailedAttempts) {
            Date lockoutEndsAt = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(lockoutMinutes));
            user.setLockoutEndsAt(lockoutEndsAt);
            appUserRepository.save(user);
            return new IllegalStateException("Too many failed login attempts. Try again in " + lockoutMinutes + " minute(s).");
        }

        appUserRepository.save(user);
        return new IllegalArgumentException(INVALID_CREDENTIALS_MESSAGE);
    }

    private void resetFailedAttempts(AppUser user) {
        if (user.getFailedLoginAttempts() == 0 && user.getLockoutEndsAt() == null) {
            return;
        }

        user.setFailedLoginAttempts(0);
        user.setLockoutEndsAt(null);
        appUserRepository.save(user);
    }
}

